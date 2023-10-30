package org.oopscraft.fintics.thread;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.arch4j.web.support.SseLogAppender;
import org.oopscraft.fintics.model.Trade;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeThreadManager {

    private final ThreadGroup tradeThreadGroup = new ThreadGroup("trade");

    private final AlarmService alarmService;

    public synchronized void startTradeThread(Trade trade) {
        synchronized (this) {
            log.info("Start TradeThread - {}", trade);
            String tradeId = trade.getTradeId();

            // check already running
            if(isTradeThreadRunning(tradeId)) {
                throw new RuntimeException(String.format("Thread Thread[%s] is already running.", trade.getName()));
            }

            // add log appender
            Context context = ((Logger)log).getLoggerContext();
            SseLogAppender sseLogAppender = new SseLogAppender(context);
            sseLogAppender.start();

            // start thread
            TradeRunnable tradeRunnable = new TradeRunnable(trade, sseLogAppender, alarmService);
            TradeThread tradeThread = new TradeThread(tradeThreadGroup, tradeRunnable, tradeId);
            tradeThread.setDaemon(true);
            tradeThread.setUncaughtExceptionHandler((thread, throwable) -> {
                log.error("[{}] {}", thread.getName(), throwable);
            });
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

            Thread[] tradeThreads = new Thread[tradeThreadGroup.activeCount()];
            tradeThreadGroup.enumerate(tradeThreads);
            for(Thread tradeThread : tradeThreads) {
                if(tradeThread.getName().equals(tradeId)) {
                    try {
                        tradeThread.interrupt();
                        tradeThread.join(10_000);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
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

}
