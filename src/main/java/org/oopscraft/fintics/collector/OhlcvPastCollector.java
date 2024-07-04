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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetOhlcvPastCollector extends AbstractCollector {

    @PersistenceContext
    private final EntityManager entityManager;

    private final PlatformTransactionManager transactionManager;

    private final TradeRepository tradeRepository;

    private final OhlcvRepository assetOhlcvRepository;

    private final FinticsProperties finticsProperties;

    private final OhlcvClient assetOhlcvClient;

    @Scheduled(initialDelay = 10_000, fixedDelay = 600_000)
    public void collect() {
        try {
            log.info("PastAssetOhlcvCollector - Start collect past asset ohlcv.");
            // expired date time
            Instant expiredDatetime = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
                    .minusMonths(finticsProperties.getDataRetentionMonths())
                    .toInstant(ZoneOffset.UTC);
            // asset
            List<TradeEntity> tradeEntities = tradeRepository.findAll();
            for (TradeEntity tradeEntity : tradeEntities) {
                Trade trade = Trade.from(tradeEntity);
                for (TradeAsset tradeAsset : trade.getTradeAssets()) {
                    try {
                        if (assetOhlcvClient.isSupported(tradeAsset)) {
                            collectPastDailyOhlcvs(tradeAsset, expiredDatetime);
                            collectPastMinuteOhlcvs(tradeAsset, expiredDatetime);
                        }
                    } catch (Throwable e) {
                        log.warn(e.getMessage());
                        sendSystemAlarm(this.getClass(), String.format("[%s] %s - %s", tradeEntity.getTradeName(), tradeAsset.getAssetName(), e.getMessage()));
                    }
                }
            }
            log.info("PastAssetOhlcvCollector - End collect past asset ohlcv");
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    void collectPastDailyOhlcvs(Asset asset, Instant expiredDatetime) {
        // date time
        Instant datetimeTo = getMinDatetime(asset.getAssetId(), Ohlcv.Type.DAILY)
                .orElse(Instant.now());
        Instant datetimeFrom = datetimeTo.minus(1, ChronoUnit.YEARS);
        // check expired date time
        if(datetimeFrom.isBefore(expiredDatetime)) {
            datetimeFrom = expiredDatetime;
        }
        // get daily ohlcvs
        List<Ohlcv> ohlcvs = assetOhlcvClient.getOhlcvs(asset, Ohlcv.Type.DAILY, datetimeFrom, datetimeTo);
        // convert and save
        List<OhlcvEntity> assetDailyOhlcvEntities = ohlcvs.stream()
                .map(ohlcv -> OhlcvEntity.builder()
                        .assetId(asset.getAssetId())
                        .datetime(ohlcv.getDatetime())
                        .type(ohlcv.getType())
                        .open(ohlcv.getOpen())
                        .high(ohlcv.getHigh())
                        .low(ohlcv.getLow())
                        .close(ohlcv.getClose())
                        .volume(ohlcv.getVolume())
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("pastAssetDailyOhlcvEntities[%s]", asset.getAssetName());
        log.info("PastOhlcvCollector - save {}:{}", unitName, assetDailyOhlcvEntities.size());
        saveEntities(unitName, assetDailyOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    void collectPastMinuteOhlcvs(Asset asset, Instant expiredDatetime) {
        Instant datetimeTo = getMinDatetime(asset.getAssetId(), Ohlcv.Type.MINUTE)
                .orElse(Instant.now());
        Instant datetimeFrom = LocalDateTime.ofInstant(datetimeTo, ZoneOffset.UTC)
                .minusMonths(1)
                .toInstant(ZoneOffset.UTC);
        // check expired date time
        if(datetimeFrom.isBefore(expiredDatetime)) {
            datetimeFrom = expiredDatetime;
        }
        // check daily min date time (in case of new IPO security)
        Instant dailyMinDatetime = getMinDatetime(asset.getAssetId(), Ohlcv.Type.DAILY).orElse(null);
        if (dailyMinDatetime != null) {
            if (datetimeFrom.isBefore(dailyMinDatetime)) {
                datetimeFrom = dailyMinDatetime;
            }
        }
        // get minute ohlcvs
        List<Ohlcv> ohlcvs = assetOhlcvClient.getOhlcvs(asset, Ohlcv.Type.MINUTE, datetimeFrom, datetimeTo);
        // convert and save
        List<OhlcvEntity> assetMinuteOhlcvEntities = ohlcvs.stream()
                .map(ohlcv -> OhlcvEntity.builder()
                        .assetId(asset.getAssetId())
                        .datetime(ohlcv.getDatetime())
                        .type(ohlcv.getType())
                        .open(ohlcv.getOpen())
                        .high(ohlcv.getHigh())
                        .low(ohlcv.getLow())
                        .close(ohlcv.getClose())
                        .volume(ohlcv.getVolume())
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("pastAssetMinuteOhlcvEntities[%s]", asset.getAssetName());
        log.info("PastOhlcvCollector - save {}:{}", unitName, assetMinuteOhlcvEntities.size());
        saveEntities(unitName, assetMinuteOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    Optional<Instant> getMinDatetime(String assetId, Ohlcv.Type type) {
        Instant minDatetime = entityManager.createQuery("select " +
                                " min(a.datetime) " +
                                " from OhlcvEntity a " +
                                " where a.assetId = :assetId " +
                                " and a.type = :type",
                        Instant.class)
                .setParameter("assetId", assetId)
                .setParameter("type", type)
                .getSingleResult();
        return Optional.ofNullable(minDatetime);
    }

}
