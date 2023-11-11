package org.oopscraft.fintics.thread;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.arch4j.web.support.SseLogAppender;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.service.TradeService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeThreadManager implements ApplicationListener<ContextClosedEvent> {

    private final ThreadGroup tradeThreadGroup = new ThreadGroup("trade");

    private final Map<String, SseLogAppender> sseLogAppenderMap = new HashMap<>();

    private final ApplicationContext applicationContext;

    public synchronized void startTradeThread(String tradeId, Integer interval) {
        synchronized (this) {
            log.info("Start TradeThread - {}", tradeId);

            // check already running
            if(isTradeThreadRunning(tradeId)) {
                throw new RuntimeException(String.format("Thread Thread[%s] is already running.", tradeId));
            }

            // add log appender
            if(!sseLogAppenderMap.containsKey(tradeId)) {
                Context context = ((Logger)log).getLoggerContext();
                SseLogAppender sseLogAppender = new SseLogAppender(context);
                sseLogAppender.start();
                sseLogAppenderMap.put(tradeId, sseLogAppender);
            }

            // start thread
            TradeRunnable tradeRunnable = new TradeRunnable(tradeId, interval, applicationContext, sseLogAppenderMap.get(tradeId));
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
                        if(sseLogAppenderMap.containsKey(tradeId)) {
                            sseLogAppenderMap.get(tradeId).stop();
                            sseLogAppenderMap.remove(tradeId);
                        }
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

    public Optional<SseLogAppender> getSseLogAppender(String tradeId) {
        if (sseLogAppenderMap.containsKey(tradeId)) {
            return Optional.of(sseLogAppenderMap.get(tradeId));
        }
        return Optional.empty();
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Shutdown all trade trade.[{}]", event);
        tradeThreadGroup.interrupt();
    }

}
