package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeThreadManager implements ApplicationListener<ContextClosedEvent> {

    private final ThreadGroup tradeThreadGroup = new ThreadGroup("trade");

    private final ApplicationContext applicationContext;

    private final SimpMessagingTemplate messagingTemplate;

    public synchronized void startTradeThread(String tradeId, Integer interval) {
        synchronized (this) {
            log.info("Start TradeThread - {}", tradeId);

            // check already running
            if(isTradeThreadRunning(tradeId)) {
                throw new RuntimeException(String.format("Thread Thread[%s] is already running.", tradeId));
            }

            // add log appender
            Context context = ((Logger)log).getLoggerContext();
            TradeLogAppender tradeLogAppender = new TradeLogAppender(tradeId, context, messagingTemplate);

            // start thread
            TradeRunnable tradeRunnable = new TradeRunnable(tradeId, interval, applicationContext, tradeLogAppender);
            TradeThread tradeThread = new TradeThread(tradeThreadGroup, tradeRunnable, tradeId);
            tradeThread.setDaemon(true);
            tradeThread.start();
        }
    }

    public synchronized void stopTradeThread(String tradeId) {
        synchronized (this) {
            log.info("Stop Trade Thread - {}", tradeId);

            // checks target exists
            if(!isTradeThreadRunning(tradeId)) {
                throw new RuntimeException(String.format("Thread Thread[%s] is not running.", tradeId));
            }

            TradeThread[] tradeThreads = new TradeThread[tradeThreadGroup.activeCount()];
            tradeThreadGroup.enumerate(tradeThreads);
            for(TradeThread tradeThread : tradeThreads) {
                if(tradeThread.getName().equals(tradeId)) {
                    try {
                        tradeThread.interrupt();
                        tradeThread.join(60_000);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public synchronized void restartTradeThread(String tradeId, Integer interval) {
        if(isTradeThreadRunning(tradeId)) {
            stopTradeThread(tradeId);
        }
        startTradeThread(tradeId, interval);
    }

    public List<TradeThread> getTradeThreads() {
        TradeThread[] tradeThreads = new TradeThread[tradeThreadGroup.activeCount()];
        tradeThreadGroup.enumerate(tradeThreads);
        return List.of(tradeThreads);
    }

    public Optional<TradeThread> getTradeThread(String tradeId) {
        for(TradeThread tradeThread : getTradeThreads()) {
            if(tradeThread.getName().equals(tradeId)) {
                return Optional.of(tradeThread);
            }
        }
        return Optional.empty();
    }

    public boolean isTradeThreadRunning(String tradeId) {
        return getTradeThread(tradeId).isPresent();
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Shutdown all trade trade.[{}]", event);
        tradeThreadGroup.interrupt();
    }

}
