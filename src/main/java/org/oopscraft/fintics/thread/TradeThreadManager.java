package org.oopscraft.fintics.thread;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.client.ClientFactory;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.Trade;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeThreadManager {

    private final Map<String, TradeThread> tradeThreadMap = new ConcurrentHashMap<>();

    private final AlarmService alarmService;

    public synchronized void startTrade(Trade trade) {
        synchronized (this) {
            log.info("start trade - {}", trade);
            String tradeId = trade.getTradeId();
            TradeThread tradeThread = new TradeThread(trade, alarmService);
            tradeThread.setDaemon(true);
            tradeThread.start();
            tradeThreadMap.put(tradeId, tradeThread);
        }
    }

    public synchronized void stopTrade(Trade trade) {
        synchronized (this) {
            log.info("stop trade - {}", trade);
            String tradeId = trade.getTradeId();
            TradeThread tradeThread = tradeThreadMap.get(tradeId);
            tradeThread.interrupt();
            tradeThreadMap.remove(tradeId);
        }
    }

    public List<Trade> getTrades() {
       return tradeThreadMap.values().stream()
               .map(TradeThread::getTrade)
               .collect(Collectors.toList());
    }

    public Optional<Trade> getTrade(String tradeId) {
        return Optional.ofNullable(tradeThreadMap.get(tradeId))
                .map(TradeThread::getTrade);
    }

    public List<AssetIndicator> getTradeAssetIndicators(String tradeId) {
        List<AssetIndicator> tradeAssetIndicator = new ArrayList<>();
        if(tradeThreadMap.containsKey(tradeId)) {
            TradeThread tradeThread = tradeThreadMap.get(tradeId);
            tradeAssetIndicator.addAll(tradeThread.getAssetIndicatorMap().values());
        }
        return tradeAssetIndicator;
    }

}
