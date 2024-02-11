package org.oopscraft.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.AssetOhlcvEntity;
import org.oopscraft.fintics.dao.AssetOhlcvRepository;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssetOhlcvCollector extends OhlcvCollector {

    private final TradeRepository tradeRepository;

    private final TradeClientFactory tradeClientFactory;

    private final AssetOhlcvRepository assetOhlcvRepository;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    @Transactional
    public void collect() {
        try {
            log.info("Start collect asset ohlcv.");
            LocalDateTime dateTime = LocalDateTime.now();
            List<TradeEntity> tradeEntities = tradeRepository.findAll();
            for (TradeEntity tradeEntity : tradeEntities) {
                try {
                    Trade trade = Trade.from(tradeEntity);
                    for (TradeAsset tradeAsset : trade.getTradeAssets()) {
                        saveAssetMinuteOhlcvs(trade, tradeAsset, dateTime);
                        saveAssetDailyOhlcvs(trade, tradeAsset, dateTime);
                    }
                } catch (Throwable e) {
                    log.warn(e.getMessage());
                }
            }
            log.info("End collect asset ohlcv");
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
        }
    }

    private void saveAssetMinuteOhlcvs(Trade trade, TradeAsset tradeAsset, LocalDateTime dateTime) throws InterruptedException {
        TradeClient tradeClient = tradeClientFactory.getObject(trade);
        List<Ohlcv> minuteOhlcvs = tradeClient.getMinuteOhlcvs(tradeAsset, dateTime);
        if(minuteOhlcvs.isEmpty()) {
            return;
        }

        // current
        List<AssetOhlcvEntity> minuteOhlcvEntities = minuteOhlcvs.stream()
                .map(ohlcv -> toAssetOhlcvEntity(tradeAsset.getAssetId(), ohlcv))
                .toList();

        // previous
        LocalDateTime dateTimeFrom = minuteOhlcvs.get(minuteOhlcvs.size()-1).getDateTime();
        LocalDateTime dateTimeTo = minuteOhlcvs.get(0).getDateTime();
        List<AssetOhlcvEntity> previousMinuteEntities = assetOhlcvRepository.findAllByAssetIdAndType(tradeAsset.getAssetId(), Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo, Pageable.unpaged());

        // save new or changed
        List<AssetOhlcvEntity> newOrChangedMinuteOhlcvEntities = extractNewOrChangedOhlcvEntities(minuteOhlcvEntities, previousMinuteEntities);
        log.info("saveAssetMinuteOhlcvs[{}]:{}", tradeAsset.getAssetId(), newOrChangedMinuteOhlcvEntities.size());
        saveEntities(newOrChangedMinuteOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    private void saveAssetDailyOhlcvs(Trade trade, TradeAsset tradeAsset, LocalDateTime dateTime) throws InterruptedException {
        TradeClient tradeClient = tradeClientFactory.getObject(trade);
        List<Ohlcv> dailyOhlcvs = tradeClient.getDailyOhlcvs(tradeAsset, dateTime);
        if(dailyOhlcvs.isEmpty()) {
            return;
        }

        // current
        List<AssetOhlcvEntity> dailyOhlcvEntities = dailyOhlcvs.stream()
                .map(ohlcv -> toAssetOhlcvEntity(tradeAsset.getAssetId(), ohlcv))
                .toList();

        // previous
        LocalDateTime dateTimeFrom = dailyOhlcvs.get(dailyOhlcvs.size()-1).getDateTime();
        LocalDateTime dateTimeTo = dailyOhlcvs.get(0).getDateTime();
        List<AssetOhlcvEntity> previousDailyOhlcvEntities = assetOhlcvRepository.findAllByAssetIdAndType(tradeAsset.getAssetId(), Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo, Pageable.unpaged());

        // save new or changed
        List<AssetOhlcvEntity> newOrChangedDailyOhlcvEntities = extractNewOrChangedOhlcvEntities(dailyOhlcvEntities, previousDailyOhlcvEntities);
        log.info("saveAssetDailyOhlcvs[{}]:{}", tradeAsset.getAssetId(), newOrChangedDailyOhlcvEntities.size());
        saveEntities(newOrChangedDailyOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    private AssetOhlcvEntity toAssetOhlcvEntity(String assetId, Ohlcv ohlcv) {
        return AssetOhlcvEntity.builder()
                .assetId(assetId)
                .dateTime(ohlcv.getDateTime())
                .type(ohlcv.getType())
                .openPrice(ohlcv.getOpenPrice())
                .highPrice(ohlcv.getHighPrice())
                .lowPrice(ohlcv.getLowPrice())
                .closePrice(ohlcv.getClosePrice())
                .volume(ohlcv.getVolume())
                .build();
    }

}
