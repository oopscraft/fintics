package org.oopscraft.fintics.thread;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.Trade;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeSynchronizer {

    private final TradeRepository tradeRepository;

    private final TradeThreadManager tradeThreadManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 1_000 * 10)
    @Transactional
    public void synchronize() {
        log.info("TradeSynchronizer.synchronize");
        List<TradeEntity> tradeEntities = tradeRepository.findAll();

        // deleted trade thread
        for(Thread tradeThread : tradeThreadManager.getTradeThreads()) {
            String tradeId = tradeThread.getName();
            boolean notExists = tradeEntities.stream()
                    .noneMatch(tradeEntity ->
                            tradeEntity.getTradeId().equals(tradeId));
            if(notExists) {
                tradeThreadManager.stopTradeThread(tradeId);
            }
        }

        // existing service monitor thread
        tradeEntities.forEach(tradeEntity -> {
            TradeRunnable tradeRunnable = tradeThreadManager.getTradeRunnable(tradeEntity.getTradeId()).orElse(null);
            if(!tradeThreadManager.isTradeThreadRunning(tradeEntity.getTradeId())) {
                if(tradeEntity.isEnabled()) {
                    tradeThreadManager.startTradeThread(Trade.from(tradeEntity));
                }
            }else{
                // when properties changed (checks overriding equals method)
                Trade newTrade = Trade.from(tradeEntity);
                if(!Objects.equals(newTrade, tradeRunnable.getTrade())) {
                    tradeThreadManager.stopTradeThread(newTrade.getTradeId());
                    if(tradeEntity.isEnabled()) {
                        tradeThreadManager.startTradeThread(newTrade);
                    }
                }
            }
        });
    }

}
