package org.oopscraft.fintics.thread;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.model.Trade;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeThreadManager {

    private final Map<String, TradeThread> tradeThreadMap = new ConcurrentHashMap<>();

    private final Map<String, TradeLogAppender> tradeLogAppenderMap = new ConcurrentHashMap<>();

    private final AlarmService alarmService;

    public synchronized void startTradeThread(Trade trade) {
        synchronized (this) {
            if(isTradeThreadRunning(trade.getTradeId())) {
                throw new RuntimeException(String.format("Thread Thread[%s] is already running.", trade.getName()));
            }
            log.info("Start TradeThread - {}", trade);
            String tradeId = trade.getTradeId();

            // add log appender
            Context context = ((Logger)log).getLoggerContext();
            TradeLogAppender tradeLogAppender = new TradeLogAppender(context);
            tradeLogAppender.start();
            tradeLogAppenderMap.put(tradeId, tradeLogAppender);

            TradeThread tradeThread = new TradeThread(trade, tradeLogAppender, alarmService);
            tradeThread.setDaemon(true);
            tradeThread.setUncaughtExceptionHandler((thread, throwable) -> {
                log.error("[{}] {}", thread.getName(), throwable.getMessage());
            });
            tradeThread.start();
            tradeThreadMap.put(tradeId, tradeThread);
        }
    }

    public synchronized void stopTradeThread(String tradeId) {
        synchronized (this) {
            if(!isTradeThreadRunning(tradeId)) {
                throw new RuntimeException(String.format("Thread Thread[%s] is not running.", tradeId));
            }
            log.info("Terminate Trade Thread - {}", tradeId);
            TradeThread tradeThread = tradeThreadMap.get(tradeId);
            tradeThread.terminate();
            tradeThreadMap.remove(tradeId);

            /// removes log appender
            tradeLogAppenderMap.remove(tradeId);
        }
    }

    public boolean isTradeThreadRunning(String tradeId) {
        return tradeThreadMap.containsKey(tradeId);
    }

    public List<TradeThread> getTradeThreads() {
        return new ArrayList<>(tradeThreadMap.values());
    }

    public TradeThread getTradeThread(String tradeId) {
        return tradeThreadMap.get(tradeId);
    }

}
