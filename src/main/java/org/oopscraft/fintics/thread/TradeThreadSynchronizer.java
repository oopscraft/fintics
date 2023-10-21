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
public class TradeThreadSynchronizer {

    private final TradeRepository tradeRepository;

    private final TradeThreadManager tradeThreadManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 1_000 * 10)
    @Transactional
    public void synchronize() {
        log.debug("== TradeThreadSynchronizer.synchronize");
        List<TradeEntity> tradeEntities = tradeRepository.findAll();

        // deleted trade thread
        for(int index = tradeThreadManager.getTradeThreads().size() - 1; index >= 0; index --) {
            TradeThread tradeThread = tradeThreadManager.getTradeThreads().get(index);
            boolean notExists = tradeEntities.stream()
                    .noneMatch(tradeEntity ->
                            tradeEntity.getTradeId().equals(tradeThread.getTrade().getTradeId()));
            if(notExists) {
                tradeThreadManager.stopTradeThread(tradeThread.getTrade().getTradeId());
            }
        }

        // existing service monitor thread
        tradeEntities.forEach(tradeEntity -> {
            TradeThread tradeThread = tradeThreadManager.getTradeThread(tradeEntity.getTradeId()).orElse(null);
            if(tradeThread == null) {
                if(tradeEntity.isEnabled()) {
                    tradeThreadManager.startTradeThread(Trade.from(tradeEntity));
                }
            }else{
                // when properties changed (checks overriding equals method)
                Trade newTrade = Trade.from(tradeEntity);
                if(!Objects.equals(newTrade, tradeThread.getTrade())) {
                    tradeThreadManager.stopTradeThread(newTrade.getTradeId());
                    if(tradeEntity.isEnabled()) {
                        tradeThreadManager.startTradeThread(newTrade);
                    }
                }
            }
        });
    }

}
