package org.oopscraft.fintics.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.Broker;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OhlcvCollector extends AbstractTask {

    private final TradeRepository tradeRepository;

    private final BrokerRepository brokerRepository;

    private final BrokerClientFactory tradeClientFactory;

    private final OhlcvRepository assetOhlcvRepository;

    private final PlatformTransactionManager transactionManager;

    /**
     * schedule collect
     */
    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    public void collect() {
        try {
            log.info("OhlcvCollector - Start collect ohlcv.");
            List<TradeEntity> tradeEntities = tradeRepository.findAll();
            for (TradeEntity tradeEntity : tradeEntities) {
                try {
                    Trade trade = Trade.from(tradeEntity);
                    for (TradeAsset tradeAsset : trade.getTradeAssets()) {
                        saveMinuteOhlcvs(trade, tradeAsset);
                        saveDailyOhlcvs(trade, tradeAsset);
                    }
                } catch (Throwable e) {
                    log.warn(e.getMessage());
                }
            }
            log.info("OhlcvCollector - End collect ohlcv");
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * saves minute ohlcvs
     * @param trade trade
     * @param tradeAsset trade asset
     */
    private void saveMinuteOhlcvs(Trade trade, TradeAsset tradeAsset) throws InterruptedException {
        // current
        Broker broker = brokerRepository.findById(trade.getBrokerId())
                .map(Broker::from)
                .orElseThrow();
        BrokerClient brokerClient = tradeClientFactory.getObject(broker);
        List<Ohlcv> minuteOhlcvs = brokerClient.getMinuteOhlcvs(tradeAsset);
        if(minuteOhlcvs.isEmpty()) {
            return;
        }
        List<OhlcvEntity> minuteOhlcvEntities = minuteOhlcvs.stream()
                .map(ohlcv -> toAssetOhlcvEntity(tradeAsset.getAssetId(), ohlcv))
                .toList();

        // previous
        LocalDateTime datetimeFrom = minuteOhlcvs.get(minuteOhlcvs.size()-1).getDateTime();
        LocalDateTime datetimeTo = minuteOhlcvs.get(0).getDateTime();
        List<OhlcvEntity> previousMinuteEntities = assetOhlcvRepository.findAllByAssetIdAndType(tradeAsset.getAssetId(), Ohlcv.Type.MINUTE, datetimeFrom, datetimeTo, Pageable.unpaged());

        // save new or changed
        List<OhlcvEntity> newOrChangedMinuteOhlcvEntities = extractNewOrChangedOhlcvEntities(minuteOhlcvEntities, previousMinuteEntities);
        String unitName = String.format("minuteOhlcvEntities[%s]", tradeAsset.getAssetName());
        log.info("OhlcvCollector - save {}:{}", unitName, newOrChangedMinuteOhlcvEntities.size());
        saveEntities(unitName, newOrChangedMinuteOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    /**
     * saves daily ohlcvs
     * @param trade trade
     * @param tradeAsset trade asset
     */
    private void saveDailyOhlcvs(Trade trade, TradeAsset tradeAsset) throws InterruptedException {
        Broker broker = brokerRepository.findById(trade.getBrokerId())
                .map(Broker::from)
                .orElseThrow();
        BrokerClient brokerClient = tradeClientFactory.getObject(broker);
        List<Ohlcv> dailyOhlcvs = brokerClient.getDailyOhlcvs(tradeAsset);
        if(dailyOhlcvs.isEmpty()) {
            return;
        }

        // current
        List<OhlcvEntity> dailyOhlcvEntities = dailyOhlcvs.stream()
                .map(ohlcv -> toAssetOhlcvEntity(tradeAsset.getAssetId(), ohlcv))
                .toList();

        // previous
        LocalDateTime datetimeFrom = dailyOhlcvs.get(dailyOhlcvs.size()-1).getDateTime();
        LocalDateTime datetimeTo = dailyOhlcvs.get(0).getDateTime();
        List<OhlcvEntity> previousDailyOhlcvEntities = assetOhlcvRepository.findAllByAssetIdAndType(tradeAsset.getAssetId(), Ohlcv.Type.DAILY, datetimeFrom, datetimeTo, Pageable.unpaged());

        // save new or changed
        List<OhlcvEntity> newOrChangedDailyOhlcvEntities = extractNewOrChangedOhlcvEntities(dailyOhlcvEntities, previousDailyOhlcvEntities);
        String unitName = String.format("dailyOhlcvEntities[%s]", tradeAsset.getAssetName());
        log.info("OhlcvCollector - save {}:{}", unitName, newOrChangedDailyOhlcvEntities.size());
        saveEntities(unitName, newOrChangedDailyOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    /**
     * convert ohlcv to entity
     * @param assetId asset id
     * @param ohlcv ohlcvs
     * @return ohlcv entity
     */
    private OhlcvEntity toAssetOhlcvEntity(String assetId, Ohlcv ohlcv) {
        return OhlcvEntity.builder()
                .assetId(assetId)
                .dateTime(ohlcv.getDateTime())
                .timeZone(ohlcv.getTimeZone())
                .type(ohlcv.getType())
                .open(ohlcv.getOpen())
                .high(ohlcv.getHigh())
                .low(ohlcv.getLow())
                .close(ohlcv.getClose())
                .volume(ohlcv.getVolume())
                .interpolated(ohlcv.isInterpolated())
                .build();
    }

    /**
     * extracts new or changed ohlcvs
     * @param ohlcvEntities ohlcv entities
     * @param previousOhlcvEntities previous ohlcv entities
     * @param <T> entity type
     * @return new or changed ohlcvs
     */
    protected <T extends OhlcvEntity> List<T> extractNewOrChangedOhlcvEntities(List<T> ohlcvEntities, List<T> previousOhlcvEntities) {
        return ohlcvEntities.stream()
                .filter(ohlcvEntity -> {
                    OhlcvEntity previousOhlcvEntity = previousOhlcvEntities.stream()
                            .filter(item -> item.getDateTime().equals(ohlcvEntity.getDateTime()))
                            .findFirst()
                            .orElse(null);
                    return previousOhlcvEntity == null || !equalsOhlcvContent(ohlcvEntity, previousOhlcvEntity);
                })
                .toList();
    }

    /**
     * check equals ohlcv content
     * @param ohlcvEntity ohlcv entity
     * @param previousOhlcvEntity previous ohlcv entity
     * @return whether new or changed
     */
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
