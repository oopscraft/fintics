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

    private final Map<String, TradeRunnable> tradeRunnableMap = new ConcurrentHashMap<>();

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

            // trade runnable
            TradeRunnable tradeRunnable = new TradeRunnable(trade, sseLogAppender, alarmService);
            tradeRunnableMap.put(tradeId, tradeRunnable);

            // start thread
            Thread tradeThread = new Thread(tradeThreadGroup, tradeRunnable, tradeId);
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
                        tradeThread.join();
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        tradeRunnableMap.remove(tradeId);
                    }
                }
            }
        }
    }

    public List<Thread> getTradeThreads() {
        Thread[] tradeThreads = new Thread[tradeThreadGroup.activeCount()];
        tradeThreadGroup.enumerate(tradeThreads);
        return List.of(tradeThreads);
    }

    public Optional<Thread> getTradeThread(String tradeId) {
        for(Thread tradeThread : getTradeThreads()) {
            if(tradeThread.getName().equals(tradeId)) {
                return Optional.of(tradeThread);
            }
        }
        return Optional.empty();
    }

    public boolean isTradeThreadRunning(String tradeId) {
        return getTradeThread(tradeId).isPresent();
    }

    public List<TradeRunnable> getTradeRunnables() {
        return new ArrayList<>(tradeRunnableMap.values());
    }

    public Optional<TradeRunnable> getTradeRunnable(String tradeId) {
        return Optional.ofNullable(tradeRunnableMap.get(tradeId));
    }

}
