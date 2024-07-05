package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.FinticsProperties;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.dao.OhlcvEntity;
import org.oopscraft.fintics.dao.OhlcvRepository;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OhlcvPastCollector extends AbstractCollector {

    @PersistenceContext
    private final EntityManager entityManager;

    private final PlatformTransactionManager transactionManager;

    private final TradeRepository tradeRepository;

    private final OhlcvRepository assetOhlcvRepository;

    private final FinticsProperties finticsProperties;

    private final OhlcvClient ohlcvClient;

    @Scheduled(initialDelay = 10_000, fixedDelay = 600_000)
    public void collect() {
        try {
            log.info("OhlcvPastCollector - Start collect past ohlcv.");
            // expired date time
            LocalDateTime expiredDateTime = LocalDateTime.now()
                    .minusMonths(finticsProperties.getDataRetentionMonths());
            // asset
            List<TradeEntity> tradeEntities = tradeRepository.findAll();
            for (TradeEntity tradeEntity : tradeEntities) {
                Trade trade = Trade.from(tradeEntity);
                for (TradeAsset tradeAsset : trade.getTradeAssets()) {
                    try {
                        if (ohlcvClient.isSupported(tradeAsset)) {
                            collectPastDailyOhlcvs(tradeAsset, expiredDateTime);
                            collectPastMinuteOhlcvs(tradeAsset, expiredDateTime);
                        }
                    } catch (Throwable e) {
                        log.warn(e.getMessage());
                        sendSystemAlarm(this.getClass(), String.format("[%s] %s - %s", tradeEntity.getTradeName(), tradeAsset.getAssetName(), e.getMessage()));
                    }
                }
            }
            log.info("OhlcvPastCollector - End collect past ohlcv");
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * collect past daily ohlcv
     * @param asset asset
     * @param expiredDateTime expired date time
     */
    void collectPastDailyOhlcvs(Asset asset, LocalDateTime expiredDateTime) {
        // date time
        LocalDateTime dateTimeTo = getMinDatetime(asset.getAssetId(), Ohlcv.Type.DAILY)
                .orElse(LocalDateTime.now());
        LocalDateTime dateTimeFrom = dateTimeTo.minusYears(1);
        // check expired date time
        if(dateTimeFrom.isBefore(expiredDateTime)) {
            dateTimeFrom = expiredDateTime;
        }
        // get daily ohlcvs
        List<Ohlcv> ohlcvs = ohlcvClient.getOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo);
        // convert and save
        List<OhlcvEntity> dailyOhlcvEntities = ohlcvs.stream()
                .map(ohlcv -> OhlcvEntity.builder()
                        .assetId(asset.getAssetId())
                        .dateTime(ohlcv.getDateTime())
                        .timeZone(ohlcv.getTimeZone())
                        .type(ohlcv.getType())
                        .open(ohlcv.getOpen())
                        .high(ohlcv.getHigh())
                        .low(ohlcv.getLow())
                        .close(ohlcv.getClose())
                        .volume(ohlcv.getVolume())
                        .interpolated(true)
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("pastAssetDailyOhlcvEntities[%s]", asset.getAssetName());
        log.info("OhlcvPastCollector - save {}:{}", unitName, dailyOhlcvEntities.size());
        saveEntities(unitName, dailyOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    /**
     * collect past minute ohlcvs
     * @param asset asset
     * @param expiredDateTime expired datetime
     */
    void collectPastMinuteOhlcvs(Asset asset, LocalDateTime expiredDateTime) {
        LocalDateTime dateTimeTo = getMinDatetime(asset.getAssetId(), Ohlcv.Type.MINUTE)
                .orElse(LocalDateTime.now());
        LocalDateTime dateTimeFrom = dateTimeTo.minusMonths(1);
        // check expired date time
        if(dateTimeFrom.isBefore(expiredDateTime)) {
            dateTimeFrom = expiredDateTime;
        }
        // check daily min date time (in case of new IPO security)
        LocalDateTime dailyMinDatetime = getMinDatetime(asset.getAssetId(), Ohlcv.Type.DAILY).orElse(null);
        if (dailyMinDatetime != null) {
            if (dateTimeFrom.isBefore(dailyMinDatetime)) {
                dateTimeFrom = dailyMinDatetime;
            }
        }
        // get minute ohlcvs
        List<Ohlcv> ohlcvs = ohlcvClient.getOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);
        // convert and save
        List<OhlcvEntity> assetMinuteOhlcvEntities = ohlcvs.stream()
                .map(ohlcv -> OhlcvEntity.builder()
                        .assetId(asset.getAssetId())
                        .dateTime(ohlcv.getDateTime())
                        .timeZone(ohlcv.getTimeZone())
                        .type(ohlcv.getType())
                        .open(ohlcv.getOpen())
                        .high(ohlcv.getHigh())
                        .low(ohlcv.getLow())
                        .close(ohlcv.getClose())
                        .volume(ohlcv.getVolume())
                        .interpolated(true)
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("pastAssetMinuteOhlcvEntities[%s]", asset.getAssetName());
        log.info("PastOhlcvCollector - save {}:{}", unitName, assetMinuteOhlcvEntities.size());
        saveEntities(unitName, assetMinuteOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    /**
     * get minimum date time
     * @param assetId asset id
     * @param type ohlcv type
     * @return return minimum datetime
     */
    Optional<LocalDateTime> getMinDatetime(String assetId, Ohlcv.Type type) {
        LocalDateTime minDatetime = entityManager.createQuery("select " +
                                " min(a.dateTime) " +
                                " from OhlcvEntity a " +
                                " where a.assetId = :assetId " +
                                " and a.type = :type",
                        LocalDateTime.class)
                .setParameter("assetId", assetId)
                .setParameter("type", type)
                .getSingleResult();
        return Optional.ofNullable(minDatetime);
    }

}
