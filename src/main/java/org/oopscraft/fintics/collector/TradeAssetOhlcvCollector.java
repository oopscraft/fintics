package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeAssetOhlcvCollector {

    private final TradeRepository tradeRepository;

    private final TradeAssetOhlcvRepository tradeAssetOhlcvRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    @Transactional
    public void collectTradeAssetOhlcv() throws InterruptedException {
        log.info("Start collect trade asset ohlcv.");
        List<TradeEntity> tradeEntities = tradeRepository.findAll();
        for(TradeEntity tradeEntity : tradeEntities) {
            Trade trade = Trade.from(tradeEntity);
            TradeClient tradeClient = TradeClientFactory.getClient(trade);
            trade.getTradeAssets().forEach(tradeAsset -> {
                saveTradeAssetOhlcv(tradeClient, tradeAsset);
            });
        }
        entityManager.clear();
    }

    @Transactional
    public void saveTradeAssetOhlcv(TradeClient tradeClient, TradeAsset tradeAsset) {
         try {
             // minutes
             List<Ohlcv> minuteOhlcvs = tradeClient.getMinuteOhlcvs(tradeAsset);
             Collections.reverse(minuteOhlcvs);
             LocalDateTime minuteLastDateTime = getLastDateTime(tradeAsset, OhlcvType.MINUTE)
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
             LocalDateTime dailyLastDateTime = getLastDateTime(tradeAsset, OhlcvType.DAILY)
                     .minusDays(2);
             List<TradeAssetOhlcvEntity> dailyOhlcvEntities = dailyOhlcvs.stream()
                     .filter(ohlcv -> ohlcv.getDateTime().isAfter(dailyLastDateTime))
                     .limit(30)
                     .map(ohlcv -> toTradeAssetOhlcvEntity(tradeAsset, ohlcv))
                     .collect(Collectors.toList());
             tradeAssetOhlcvRepository.saveAllAndFlush(dailyOhlcvEntities);
         }catch(InterruptedException ignored){}
    }

    private LocalDateTime getLastDateTime(TradeAsset tradeAsset, OhlcvType ohlcvType) {
        List<TradeAssetOhlcvEntity> latestRow = entityManager.createQuery(
                "select a from TradeAssetOhlcvEntity a " +
                        " where a.tradeId = :tradeId " +
                        " and a.symbol = :symbol " +
                        " and a.ohlcvType = :ohlcvType" +
                        " order by a.dateTime desc",
                        TradeAssetOhlcvEntity.class)
                .setParameter("tradeId", tradeAsset.getTradeId())
                .setParameter("symbol", tradeAsset.getSymbol())
                .setParameter("ohlcvType", ohlcvType)
                .setMaxResults(1)
                .getResultList();
        if(latestRow.isEmpty()) {
            return LocalDateTime.of(1,1,1,1,1,1);
        }else{
            return latestRow.get(0).getDateTime();
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


}
