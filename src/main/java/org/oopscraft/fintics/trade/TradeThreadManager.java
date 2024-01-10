package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.model.Trade;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeThreadManager implements ApplicationListener<ContextClosedEvent> {

    private final ThreadGroup tradeThreadGroup = new ThreadGroup("trade");

    private final Map<String,TradeThread> tradeThreadMap = new ConcurrentHashMap<>();

    private final ApplicationContext applicationContext;

    private final SimpMessagingTemplate messagingTemplate;

    public synchronized void startTradeThread(Trade trade) {
        synchronized (this) {
            log.info("Start TradeThread - {}", trade.getId());

            // check already running
            if(isTradeThreadRunning(trade.getId())) {
                throw new RuntimeException(String.format("Thread Thread[%s] is already running.", trade.getId()));
            }

            // add log appender
            Context context = ((Logger)log).getLoggerContext();
            TradeLogAppender tradeLogAppender = new TradeLogAppender(trade, context, messagingTemplate);

            // start thread
            TradeRunnable tradeRunnable = TradeRunnable.builder()
                    .id(trade.getId())
                    .interval(trade.getInterval())
                    .applicationContext(applicationContext)
                    .tradeLogAppender(tradeLogAppender)
                    .build();
            TradeThread tradeThread = new TradeThread(tradeThreadGroup, tradeRunnable, trade.getId());
            tradeThread.setDaemon(true);
            tradeThread.start();
            tradeThreadMap.put(trade.getId(), tradeThread);
        }
    }

    public synchronized void stopTradeThread(String id) {
        synchronized (this) {
            log.info("Stop Trade Thread - {}", id);

            // checks target exists
            if(!isTradeThreadRunning(id)) {
                throw new RuntimeException(String.format("Thread Thread[%s] is not running.", id));
            }

            // interrupt thread
            getTradeThread(id).ifPresent(tradeThread -> {
                try {
                    tradeThread.interrupt();
                    tradeThread.join(60_000);
                    tradeThreadMap.remove(id);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public synchronized void restartTradeThread(Trade trade) {
        if(isTradeThreadRunning(trade.getId())) {
            stopTradeThread(trade.getId());
        }
        startTradeThread(trade);
    }

    public List<TradeThread> getTradeThreads() {
        return new ArrayList<>(tradeThreadMap.values());
    }

    public Optional<TradeThread> getTradeThread(String id) {
        if(tradeThreadMap.containsKey(id)) {
            return Optional.of(tradeThreadMap.get(id));
        }else{
            return Optional.empty();
        }
    }

    public boolean isTradeThreadRunning(String id) {
        return getTradeThread(id).isPresent();
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Shutdown all trade trade.[{}]", event);
        tradeThreadGroup.interrupt();
    }

}
