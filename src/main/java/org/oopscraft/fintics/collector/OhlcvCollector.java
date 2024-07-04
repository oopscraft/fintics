package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.Broker;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetOhlcvCollector extends AbstractCollector {

    private final TradeRepository tradeRepository;

    private final BrokerRepository brokerRepository;

    private final BrokerClientFactory tradeClientFactory;

    private final OhlcvRepository assetOhlcvRepository;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    public void collect() {
        try {
            log.info("AssetOhlcvCollector - Start collect asset ohlcv.");
            List<TradeEntity> tradeEntities = tradeRepository.findAll();
            for (TradeEntity tradeEntity : tradeEntities) {
                try {
                    Trade trade = Trade.from(tradeEntity);
                    for (TradeAsset tradeAsset : trade.getTradeAssets()) {
                        saveMinuteAssetOhlcvs(trade, tradeAsset);
                        saveDailyAssetOhlcvs(trade, tradeAsset);
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

    private void saveMinuteAssetOhlcvs(Trade trade, TradeAsset tradeAsset) throws InterruptedException {
        // current
        Broker broker = brokerRepository.findById(trade.getBrokerId())
                .map(Broker::from)
                .orElseThrow();
        BrokerClient brokerClient = tradeClientFactory.getObject(broker);
        List<Ohlcv> minuteOhlcvs = brokerClient.getMinuteAssetOhlcvs(tradeAsset);
        if(minuteOhlcvs.isEmpty()) {
            return;
        }
        List<OhlcvEntity> minuteOhlcvEntities = minuteOhlcvs.stream()
                .map(ohlcv -> toAssetOhlcvEntity(tradeAsset.getAssetId(), ohlcv))
                .toList();

        // previous
        Instant datetimeFrom = minuteOhlcvs.get(minuteOhlcvs.size()-1).getDatetime();
        Instant datetimeTo = minuteOhlcvs.get(0).getDatetime();
        List<OhlcvEntity> previousMinuteEntities = assetOhlcvRepository.findAllByAssetIdAndType(tradeAsset.getAssetId(), Ohlcv.Type.MINUTE, datetimeFrom, datetimeTo, Pageable.unpaged());

        // save new or changed
        List<OhlcvEntity> newOrChangedMinuteOhlcvEntities = extractNewOrChangedOhlcvEntities(minuteOhlcvEntities, previousMinuteEntities);
        String unitName = String.format("assetMinuteOhlcvEntities[%s]", tradeAsset.getAssetName());
        log.info("AssetOhlcvCollector - save {}:{}", unitName, newOrChangedMinuteOhlcvEntities.size());
        saveEntities(unitName, newOrChangedMinuteOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    private void saveDailyAssetOhlcvs(Trade trade, TradeAsset tradeAsset) throws InterruptedException {
        Broker broker = brokerRepository.findById(trade.getBrokerId())
                .map(Broker::from)
                .orElseThrow();
        BrokerClient brokerClient = tradeClientFactory.getObject(broker);
        List<Ohlcv> dailyOhlcvs = brokerClient.getDailyAssetOhlcvs(tradeAsset);
        if(dailyOhlcvs.isEmpty()) {
            return;
        }

        // current
        List<OhlcvEntity> dailyOhlcvEntities = dailyOhlcvs.stream()
                .map(ohlcv -> toAssetOhlcvEntity(tradeAsset.getAssetId(), ohlcv))
                .toList();

        // previous
        Instant datetimeFrom = dailyOhlcvs.get(dailyOhlcvs.size()-1).getDatetime();
        Instant datetimeTo = dailyOhlcvs.get(0).getDatetime();
        List<OhlcvEntity> previousDailyOhlcvEntities = assetOhlcvRepository.findAllByAssetIdAndType(tradeAsset.getAssetId(), Ohlcv.Type.DAILY, datetimeFrom, datetimeTo, Pageable.unpaged());

        // save new or changed
        List<OhlcvEntity> newOrChangedDailyOhlcvEntities = extractNewOrChangedOhlcvEntities(dailyOhlcvEntities, previousDailyOhlcvEntities);
        String unitName = String.format("assetDailyOhlcvEntities[%s]", tradeAsset.getAssetName());
        log.info("AssetOhlcvCollector - save {}:{}", unitName, newOrChangedDailyOhlcvEntities.size());
        saveEntities(unitName, newOrChangedDailyOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    private OhlcvEntity toAssetOhlcvEntity(String assetId, Ohlcv ohlcv) {
        return OhlcvEntity.builder()
                .assetId(assetId)
                .datetime(ohlcv.getDatetime())
                .type(ohlcv.getType())
                .open(ohlcv.getOpen())
                .high(ohlcv.getHigh())
                .low(ohlcv.getLow())
                .close(ohlcv.getClose())
                .volume(ohlcv.getVolume())
                .build();
    }

    protected <T extends OhlcvEntity> List<T> extractNewOrChangedOhlcvEntities(List<T> ohlcvEntities, List<T> previousOhlcvEntities) {
        return ohlcvEntities.stream()
                .filter(ohlcvEntity -> {
                    OhlcvEntity previousOhlcvEntity = previousOhlcvEntities.stream()
                            .filter(item -> item.getDatetime().equals(ohlcvEntity.getDatetime()))
                            .findFirst()
                            .orElse(null);
                    return previousOhlcvEntity == null || !equalsOhlcvContent(ohlcvEntity, previousOhlcvEntity);
                })
                .toList();
    }

    protected boolean equalsOhlcvContent(OhlcvEntity ohlcvEntity, OhlcvEntity previousOhlcvEntity) {
        int priceScale = Math.min(Optional.ofNullable(ohlcvEntity.getClose()).map(BigDecimal::scale).orElse(0), Optional.ofNullable(previousOhlcvEntity.getClose()).map(BigDecimal::scale).orElse(0));
        int volumeScale = Math.min(Optional.ofNullable(ohlcvEntity.getVolume()).map(BigDecimal::scale).orElse(0), Optional.ofNullable(previousOhlcvEntity.getVolume()).map(BigDecimal::scale).orElse(0));

        BigDecimal ourOpen = Optional.ofNullable(ohlcvEntity.getOpen()).map(closePrice -> closePrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal ourHigh = Optional.ofNullable(ohlcvEntity.getHigh()).map(highPrice -> highPrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal ourLow = Optional.ofNullable(ohlcvEntity.getLow()).map(lowPrice -> lowPrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal ourClose = Optional.ofNullable(ohlcvEntity.getClose()).map(closePrice -> closePrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal ourVolume = Optional.ofNullable(ohlcvEntity.getVolume()).map(volume -> volume.setScale(volumeScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);

        BigDecimal theirOpen = Optional.ofNullable(previousOhlcvEntity.getOpen()).map(closePrice -> closePrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal theirHigh = Optional.ofNullable(previousOhlcvEntity.getHigh()).map(highPrice -> highPrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal theirLow = Optional.ofNullable(previousOhlcvEntity.getLow()).map(lowPrice -> lowPrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal theirClose = Optional.ofNullable(previousOhlcvEntity.getClose()).map(closePrice -> closePrice.setScale(priceScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);
        BigDecimal theirVolume = Optional.ofNullable(previousOhlcvEntity.getVolume()).map(volume -> volume.setScale(volumeScale, RoundingMode.FLOOR)).orElse(BigDecimal.ZERO);

        return ourOpen.compareTo(theirOpen) == 0
                && ourHigh.compareTo(theirHigh) == 0
                && ourLow.compareTo(theirLow) == 0
                && ourClose.compareTo(theirClose) == 0
                && ourVolume.compareTo(theirVolume) == 0;
    }

}
