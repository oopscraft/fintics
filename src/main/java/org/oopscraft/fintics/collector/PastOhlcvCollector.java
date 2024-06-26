package org.oopscraft.fintics.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.FinticsProperties;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.IndiceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PastOhlcvCollector extends OhlcvCollector {

    @PersistenceContext
    private final EntityManager entityManager;

    private final ObjectMapper objectMapper;

    private final PlatformTransactionManager transactionManager;

    private final TradeRepository tradeRepository;

    private final AssetOhlcvRepository assetOhlcvRepository;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    private final FinticsProperties finticsProperties;

    private final IndiceService indiceService;

    private final OhlcvClient ohlcvClient;

    @Scheduled(initialDelay = 10_000, fixedDelay = 600_000)
    public void collect() {
        try {
            log.info("PastOhlcvCollector - Start collect past asset ohlcv.");
            // expired date time
            LocalDateTime expiredDateTime = LocalDateTime.now().minusMonths(finticsProperties.getOhlcvRetentionMonths());
            // asset
            List<TradeEntity> tradeEntities = tradeRepository.findAll();
            for (TradeEntity tradeEntity : tradeEntities) {
                Trade trade = Trade.from(tradeEntity);
                for (TradeAsset tradeAsset : trade.getTradeAssets()) {
                    try {
                        if (ohlcvClient.isSupported(tradeAsset)) {
                            collectPastAssetDailyOhlcvs(tradeAsset, expiredDateTime);
                            collectPastAssetMinuteOhlcvs(tradeAsset, expiredDateTime);
                        }
                    } catch (Throwable e) {
                        log.warn(e.getMessage());
                        sendSystemAlarm(this.getClass(), String.format("[%s] %s - %s", tradeEntity.getTradeName(), tradeAsset.getAssetName(), e.getMessage()));
                    }
                }
            }
            // indice
            List<Indice> indices = indiceService.getIndices();
            for (Indice indice : indices) {
                try {
                    if (ohlcvClient.isSupported(indice)) {
                        collectPastIndiceDailyOhlcvs(indice, expiredDateTime);
                        collectPastIndiceMinuteOhlcvs(indice, expiredDateTime);
                    }
                } catch (Throwable e) {
                    log.warn(e.getMessage());
                    sendSystemAlarm(this.getClass(), String.format("[%s] %s - %s", indice.getIndiceId(), indice.getIndiceName(), e.getMessage()));
                }
            }
            log.info("PastOhlcvCollector - End collect past asset ohlcv");
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    void collectPastAssetDailyOhlcvs(Asset asset, LocalDateTime expiredDateTime) {
        // date time
        LocalDateTime dateTimeTo = getAssetMinDateTime(asset.getAssetId(), Ohlcv.Type.DAILY)
                .orElse(LocalDateTime.now());
        LocalDateTime dateTimeFrom = dateTimeTo.minusYears(1);
        // check expired date time
        if(dateTimeFrom.isBefore(expiredDateTime)) {
            dateTimeFrom = expiredDateTime;
        }
        // get daily ohlcvs
        List<Ohlcv> ohlcvs = ohlcvClient.getAssetOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo);
        // convert and save
        List<AssetOhlcvEntity> assetDailyOhlcvEntities = ohlcvs.stream()
                .map(ohlcv -> AssetOhlcvEntity.builder()
                        .assetId(asset.getAssetId())
                        .dateTime(ohlcv.getDateTime())
                        .type(ohlcv.getType())
                        .openPrice(ohlcv.getOpenPrice())
                        .highPrice(ohlcv.getHighPrice())
                        .lowPrice(ohlcv.getLowPrice())
                        .closePrice(ohlcv.getClosePrice())
                        .volume(ohlcv.getVolume())
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("pastAssetDailyOhlcvEntities[%s]", asset.getAssetName());
        log.info("PastOhlcvCollector - save {}:{}", unitName, assetDailyOhlcvEntities.size());
        saveEntities(unitName, assetDailyOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    void collectPastAssetMinuteOhlcvs(Asset asset, LocalDateTime expiredDateTime) {
        LocalDateTime dateTimeTo = getAssetMinDateTime(asset.getAssetId(), Ohlcv.Type.MINUTE)
                .orElse(LocalDateTime.now());
        LocalDateTime dateTimeFrom = dateTimeTo.minusMonths(1);
        // check expired date time
        if(dateTimeFrom.isBefore(expiredDateTime)) {
            dateTimeFrom = expiredDateTime;
        }
        // check daily min date time (in case of new IPO security)
        LocalDateTime dailyMinDateTime = getAssetMinDateTime(asset.getAssetId(), Ohlcv.Type.DAILY).orElse(null);
        if (dailyMinDateTime != null) {
            if (dateTimeFrom.isBefore(dailyMinDateTime)) {
                dateTimeFrom = dailyMinDateTime;
            }
        }
        // get minute ohlcvs
        List<Ohlcv> ohlcvs = ohlcvClient.getAssetOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);
        // convert and save
        List<AssetOhlcvEntity> assetMinuteOhlcvEntities = ohlcvs.stream()
                .map(ohlcv -> AssetOhlcvEntity.builder()
                        .assetId(asset.getAssetId())
                        .dateTime(ohlcv.getDateTime())
                        .type(ohlcv.getType())
                        .openPrice(ohlcv.getOpenPrice())
                        .highPrice(ohlcv.getHighPrice())
                        .lowPrice(ohlcv.getLowPrice())
                        .closePrice(ohlcv.getClosePrice())
                        .volume(ohlcv.getVolume())
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("pastAssetMinuteOhlcvEntities[%s]", asset.getAssetName());
        log.info("PastOhlcvCollector - save {}:{}", unitName, assetMinuteOhlcvEntities.size());
        saveEntities(unitName, assetMinuteOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    void collectPastIndiceDailyOhlcvs(Indice indice, LocalDateTime expiredDateTime) {
        // defines
        LocalDateTime dateTimeTo = getIndiceMinDateTime(indice.getIndiceId(), Ohlcv.Type.DAILY)
                .orElse(LocalDateTime.now());
        LocalDateTime dateTimeFrom = dateTimeTo.minusYears(1);
        // check expired date time
        if(dateTimeFrom.isBefore(expiredDateTime)) {
            dateTimeFrom = expiredDateTime;
        }
        // get daily ohlcvs
        List<Ohlcv> ohlcvs = ohlcvClient.getIndiceOhlcvs(indice, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo);
        // convert and save
        List<IndiceOhlcvEntity> indiceDailyOhlcvEntities = ohlcvs.stream()
                .map(ohlcv -> IndiceOhlcvEntity.builder()
                        .indiceId(indice.getIndiceId())
                        .dateTime(ohlcv.getDateTime())
                        .type(ohlcv.getType())
                        .openPrice(ohlcv.getOpenPrice())
                        .highPrice(ohlcv.getHighPrice())
                        .lowPrice(ohlcv.getLowPrice())
                        .closePrice(ohlcv.getClosePrice())
                        .volume(ohlcv.getVolume())
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("pastIndiceDailyOhlcvEntities[%s]", indice.getIndiceId());
        log.info("PastOhlcvCollector - save {}:{}", unitName, indiceDailyOhlcvEntities.size());
        saveEntities(unitName, indiceDailyOhlcvEntities, transactionManager, indiceOhlcvRepository);
    }

    void collectPastIndiceMinuteOhlcvs(Indice indice, LocalDateTime expiredDateTime) {
        // defines
        LocalDateTime dateTimeTo = getIndiceMinDateTime(indice.getIndiceId(), Ohlcv.Type.MINUTE)
                .orElse(LocalDateTime.now());
        LocalDateTime dateTimeFrom = dateTimeTo.minusMonths(1);
        // check expired date time
        if(dateTimeFrom.isBefore(expiredDateTime)) {
            dateTimeFrom = expiredDateTime;
        }
        // check daily min date time (in case of new IPO security)
        LocalDateTime dailyMinDateTime = getIndiceMinDateTime(indice.getIndiceId(), Ohlcv.Type.DAILY).orElse(null);
        if (dailyMinDateTime != null) {
            if (dateTimeFrom.isBefore(dailyMinDateTime)) {
                dateTimeFrom = dailyMinDateTime;
            }
        }
        // get minute ohlcvs
        List<Ohlcv> ohlcvs = ohlcvClient.getIndiceOhlcvs(indice, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);
        // convert and save
        List<IndiceOhlcvEntity> indiceMinuteOhlcvEntities = ohlcvs.stream()
                .map(ohlcv -> IndiceOhlcvEntity.builder()
                        .indiceId(indice.getIndiceId())
                        .dateTime(ohlcv.getDateTime())
                        .type(ohlcv.getType())
                        .openPrice(ohlcv.getOpenPrice())
                        .highPrice(ohlcv.getHighPrice())
                        .lowPrice(ohlcv.getLowPrice())
                        .closePrice(ohlcv.getClosePrice())
                        .volume(ohlcv.getVolume())
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("pastIndiceMinuteOhlcvEntities[%s]", indice.getIndiceId());
        log.info("PastOhlcvCollector - save {}:{}", unitName, indiceMinuteOhlcvEntities.size());
        saveEntities(unitName, indiceMinuteOhlcvEntities, transactionManager, indiceOhlcvRepository);
    }

    Optional<LocalDateTime> getAssetMinDateTime(String assetId, Ohlcv.Type type) {
        LocalDateTime minDateTime = entityManager.createQuery("select " +
                                " min(a.dateTime) " +
                                " from AssetOhlcvEntity a " +
                                " where a.assetId = :assetId " +
                                " and a.type = :type",
                        LocalDateTime.class)
                .setParameter("assetId", assetId)
                .setParameter("type", type)
                .getSingleResult();
        return Optional.ofNullable(minDateTime);
    }

    Optional<LocalDateTime> getIndiceMinDateTime(Indice.Id indiceId, Ohlcv.Type type) {
        LocalDateTime minDateTime = entityManager.createQuery("select " +
                                " min(a.dateTime) " +
                                " from IndiceOhlcvEntity a " +
                                " where a.indiceId = :indiceId" +
                                " and a.type = :type",
                        LocalDateTime.class)
                .setParameter("indiceId", indiceId)
                .setParameter("type", type)
                .getSingleResult();
        return Optional.ofNullable(minDateTime);
    }

}
