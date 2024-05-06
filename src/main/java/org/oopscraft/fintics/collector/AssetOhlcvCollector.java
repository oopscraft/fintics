package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetOhlcvCollector extends OhlcvCollector {

    private final TradeRepository tradeRepository;

    private final BrokerRepository brokerRepository;

    private final BrokerClientFactory tradeClientFactory;

    private final AssetOhlcvRepository assetOhlcvRepository;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    public void collect() {
        try {
            log.info("AssetOhlcvCollector - Start collect asset ohlcv.");
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
            log.info("AssetOhlcvCollector - End collect asset ohlcv");
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void saveAssetMinuteOhlcvs(Trade trade, TradeAsset tradeAsset, LocalDateTime dateTime) throws InterruptedException {
        // current
        Broker broker = brokerRepository.findById(trade.getBrokerId())
                .map(Broker::from)
                .orElseThrow();
        BrokerClient brokerClient = tradeClientFactory.getObject(broker);
        List<Ohlcv> minuteOhlcvs = brokerClient.getMinuteOhlcvs(tradeAsset, dateTime);
        if(minuteOhlcvs.isEmpty()) {
            return;
        }
        List<AssetOhlcvEntity> minuteOhlcvEntities = minuteOhlcvs.stream()
                .map(ohlcv -> toAssetOhlcvEntity(tradeAsset.getAssetId(), ohlcv))
                .toList();

        // previous
        LocalDateTime dateTimeFrom = minuteOhlcvs.get(minuteOhlcvs.size()-1).getDateTime();
        LocalDateTime dateTimeTo = minuteOhlcvs.get(0).getDateTime();
        List<AssetOhlcvEntity> previousMinuteEntities = assetOhlcvRepository.findAllByAssetIdAndType(tradeAsset.getAssetId(), Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo, Pageable.unpaged());

        // save new or changed
        List<AssetOhlcvEntity> newOrChangedMinuteOhlcvEntities = extractNewOrChangedOhlcvEntities(minuteOhlcvEntities, previousMinuteEntities);
        String unitName = String.format("assetMinuteOhlcvEntities[%s]", tradeAsset.getAssetName());
        log.info("AssetOhlcvCollector - save {}:{}", unitName, newOrChangedMinuteOhlcvEntities.size());
        saveEntities(unitName, newOrChangedMinuteOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    private void saveAssetDailyOhlcvs(Trade trade, TradeAsset tradeAsset, LocalDateTime dateTime) throws InterruptedException {
        Broker broker = brokerRepository.findById(trade.getBrokerId())
                .map(Broker::from)
                .orElseThrow();
        BrokerClient brokerClient = tradeClientFactory.getObject(broker);
        List<Ohlcv> dailyOhlcvs = brokerClient.getDailyOhlcvs(tradeAsset, dateTime);
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
        String unitName = String.format("assetDailyOhlcvEntities[%s]", tradeAsset.getAssetName());
        log.info("AssetOhlcvCollector - save {}:{}", unitName, newOrChangedDailyOhlcvEntities.size());
        saveEntities(unitName, newOrChangedDailyOhlcvEntities, transactionManager, assetOhlcvRepository);
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
