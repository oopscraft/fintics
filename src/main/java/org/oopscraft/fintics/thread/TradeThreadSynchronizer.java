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
        List<Trade> trades = tradeThreadManager.getTrades();

        // deleted trade thread
        for(int index = trades.size() - 1; index >= 0; index --) {
            Trade trade = trades.get(index);
            boolean notExists = tradeEntities.stream()
                    .noneMatch(tradeEntity ->
                            tradeEntity.getTradeId().equals(trade.getTradeId()));
            if(notExists) {
                tradeThreadManager.stopTrade(trade);
            }
        }

        // existing service monitor thread
        tradeEntities.forEach(tradeEntity -> {
            Trade trade = tradeThreadManager.getTrade(tradeEntity.getTradeId()).orElse(null);
            if(trade == null) {
                if(tradeEntity.isEnabled()) {
                    tradeThreadManager.startTrade(Trade.from(tradeEntity));
                }
            }else{
                // when properties changed (checks overriding equals method)
                Trade newTrade = Trade.from(tradeEntity);
                if(!Objects.equals(newTrade, trade)) {
                    tradeThreadManager.stopTrade(trade);
                    if(tradeEntity.isEnabled()) {
                        tradeThreadManager.startTrade(newTrade);
                    }
                }
            }
        });
    }

}
