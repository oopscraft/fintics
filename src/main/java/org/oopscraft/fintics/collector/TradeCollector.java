package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.TradeAssetOhlcvEntity;
import org.oopscraft.fintics.dao.TradeAssetOhlcvRepository;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.OhlcvType;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TradeCollector {

    @Value("${fintics.collector.trade-collector.ohlcv-retention-months:1}")
    private Integer ohlcvRetentionMonths = 1;

    private final TradeRepository tradeRepository;

    private final TradeAssetOhlcvRepository tradeAssetOhlcvRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    @Transactional
    public void collectTradeAssetOhlcv() {
        log.info("Start collect trade asset ohlcv.");
        List<TradeEntity> tradeEntities = tradeRepository.findAll();
        for(TradeEntity tradeEntity : tradeEntities) {
            TransactionStatus transactionStatus = null;
            try {
                Trade trade = Trade.from(tradeEntity);
                TradeClient tradeClient = TradeClientFactory.getClient(trade);
                trade.getTradeAssets().forEach(tradeAsset -> {
                    saveTradeAssetOhlcv(tradeClient, tradeAsset);
                    deletePastRetentionOhlcv(tradeAsset);
                });
            }catch(Throwable e){
                log.warn(e.getMessage());
            }
        }
        log.info("End collect trade asset ohlcv");
    }

    private void saveTradeAssetOhlcv(TradeClient tradeClient, TradeAsset tradeAsset) {
         try {
             // minutes
             List<Ohlcv> minuteOhlcvs = tradeClient.getMinuteOhlcvs(tradeAsset);
             Collections.reverse(minuteOhlcvs);
             LocalDateTime minuteLastDateTime = tradeAssetOhlcvRepository.findMaxDateTimeBySymbolAndOhlcvType(tradeAsset.getTradeId(), tradeAsset.getSymbol(), OhlcvType.MINUTE)
                     .orElse(getExpiredDateTime())
                     .minusMinutes(2);
             List<TradeAssetOhlcvEntity> minuteTradeAssetOhlcvEntities = minuteOhlcvs.stream()
                     .filter(ohlcv -> ohlcv.getDateTime().isAfter(minuteLastDateTime))
                     .limit(30)
                     .map(ohlcv -> toTradeAssetOhlcvEntity(tradeAsset, ohlcv))
                     .collect(Collectors.toList());
             tradeAssetOhlcvRepository.saveAllAndFlush(minuteTradeAssetOhlcvEntities);

             // daily
             List<Ohlcv> dailyOhlcvs = tradeClient.getDailyOhlcvs(tradeAsset);
             Collections.reverse(dailyOhlcvs);
             LocalDateTime dailyLastDateTime = tradeAssetOhlcvRepository.findMaxDateTimeBySymbolAndOhlcvType(tradeAsset.getTradeId(), tradeAsset.getSymbol(), OhlcvType.DAILY)
                     .orElse(getExpiredDateTime())
                     .minusDays(2);
             List<TradeAssetOhlcvEntity> dailyOhlcvEntities = dailyOhlcvs.stream()
                     .filter(ohlcv -> ohlcv.getDateTime().isAfter(dailyLastDateTime))
                     .limit(30)
                     .map(ohlcv -> toTradeAssetOhlcvEntity(tradeAsset, ohlcv))
                     .collect(Collectors.toList());
             tradeAssetOhlcvRepository.saveAllAndFlush(dailyOhlcvEntities);
         }catch(InterruptedException e){
             log.warn(e.getMessage());
         }
    }

    private TradeAssetOhlcvEntity toTradeAssetOhlcvEntity(TradeAsset tradeAsset, Ohlcv ohlcv) {
        return TradeAssetOhlcvEntity.builder()
                .tradeId(tradeAsset.getTradeId())
                .symbol(tradeAsset.getSymbol())
                .dateTime(ohlcv.getDateTime())
                .ohlcvType(ohlcv.getOhlcvType())
                .openPrice(ohlcv.getOpenPrice())
                .highPrice(ohlcv.getHighPrice())
                .lowPrice(ohlcv.getLowPrice())
                .closePrice(ohlcv.getClosePrice())
                .volume(ohlcv.getVolume())
                .build();
    }

    private LocalDateTime getExpiredDateTime() {
        return LocalDateTime.now().minusMonths(ohlcvRetentionMonths);
    }

    private void deletePastRetentionOhlcv(TradeAsset tradeAsset) {
        entityManager.createQuery(
                        "delete" +
                                " from TradeAssetOhlcvEntity" +
                                " where tradeId = :tradeId " +
                                " and symbol = :symbol " +
                                " and dateTime < :expiredDateTime")
                .setParameter("tradeId", tradeAsset.getTradeId())
                .setParameter("symbol", tradeAsset.getSymbol())
                .setParameter("expiredDateTime", getExpiredDateTime())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

}
