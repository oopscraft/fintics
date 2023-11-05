package org.oopscraft.fintics.thread;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.dao.TradeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeSynchronizer {

    private final TradeRepository tradeRepository;

    private final TradeThreadManager tradeThreadManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 10_000)
    @Transactional(readOnly = true)
    public void synchronize() {
        log.info("Start TradeSynchronizer.synchronize.");
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

        // start trade thread if enabled
        for(TradeEntity tradeEntity : tradeEntities) {
            String tradeId = tradeEntity.getTradeId();
            boolean enabled = tradeEntity.isEnabled();
            Integer interval = tradeEntity.getInterval();
            if(enabled) {
                TradeThread tradeThread = tradeThreadManager.getTradeThread(tradeId).orElse(null);
                if(tradeThread == null) {
                    tradeThreadManager.startTradeThread(tradeId, interval);
                    continue;
                }
                if(tradeThread.getTradeRunnable().getInterval().intValue() != interval.intValue()) {
                    tradeThreadManager.restartTradeThread(tradeId, interval);
                }
            }else{
                if(tradeThreadManager.isTradeThreadRunning(tradeId)) {
                    tradeThreadManager.stopTradeThread(tradeId);
                }
            }
        }
        log.info("End TradeSynchronizer.synchronize.");
    }

}
