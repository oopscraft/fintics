package org.oopscraft.fintics.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.FinticsProperties;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.IndiceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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

    @Scheduled(cron = "0 0 18 * * *")
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
                        collectPastAssetMinuteOhlcvs(tradeAsset, expiredDateTime);
                        collectPastAssetDailyOhlcvs(tradeAsset, expiredDateTime);
                    } catch (Throwable e) {
                        log.warn(e.getMessage());
                        sendSystemAlarm(this.getClass(), String.format("%s - %s", tradeEntity.getTradeName(), e.getMessage()));
                    }
                }
            }
            // indice
            List<Indice> indices = indiceService.getIndices();
            for (Indice indice : indices) {
                try {
                    collectPastIndiceMinuteOhlcvs(indice, expiredDateTime);
                    collectPastIndiceDailyOhlcvs(indice, expiredDateTime);
                } catch (Throwable e) {
                    log.warn(e.getMessage());
                    sendSystemAlarm(this.getClass(), String.format("%s - %s", indice.getIndiceId(), e.getMessage()));
                }
            }
            log.info("PastOhlcvCollector - End collect past asset ohlcv");
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    void collectPastAssetMinuteOhlcvs(Asset asset, LocalDateTime expiredDateTime) {
        LocalDateTime dateTimeTo = getAssetMinDateTime(asset.getAssetId(), Ohlcv.Type.MINUTE)
                .orElse(LocalDateTime.now());
        LocalDateTime dateTimeFrom = dateTimeTo.minusWeeks(1);
        // check expired date time
        if(dateTimeFrom.isBefore(expiredDateTime)) {
            dateTimeFrom = expiredDateTime;
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
                        .interpolated(ohlcv.isInterpolated())
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("pastAssetMinuteOhlcvEntities[%s]", asset.getAssetName());
        log.info("PastOhlcvCollector - save {}:{}", unitName, assetMinuteOhlcvEntities.size());
        saveEntities(unitName, assetMinuteOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    void collectPastAssetDailyOhlcvs(Asset asset, LocalDateTime expiredDateTime) {
        // date time
        LocalDateTime dateTimeTo = getAssetMinDateTime(asset.getAssetId(), Ohlcv.Type.DAILY)
                .orElse(LocalDateTime.now());
        LocalDateTime dateTimeFrom = dateTimeTo.minusMonths(1);
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
                        .interpolated(ohlcv.isInterpolated())
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("pastAssetDailyOhlcvEntities[%s]", asset.getAssetName());
        log.info("PastOhlcvCollector - save {}:{}", unitName, assetDailyOhlcvEntities.size());
        saveEntities(unitName, assetDailyOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    void collectPastIndiceMinuteOhlcvs(Indice indice, LocalDateTime expiredDateTime) {
        // defines
        LocalDateTime dateTimeTo = getIndiceMinDateTime(indice.getIndiceId(), Ohlcv.Type.MINUTE)
                .orElse(LocalDateTime.now());
        LocalDateTime dateTimeFrom = dateTimeTo.minusWeeks(1);
        // check expired date time
        if(dateTimeFrom.isBefore(expiredDateTime)) {
            dateTimeFrom = expiredDateTime;
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
                        .interpolated(ohlcv.isInterpolated())
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("pastIndiceMinuteOhlcvEntities[%s]", indice.getIndiceId());
        log.info("PastOhlcvCollector - save {}:{}", unitName, indiceMinuteOhlcvEntities.size());
        saveEntities(unitName, indiceMinuteOhlcvEntities, transactionManager, indiceOhlcvRepository);
    }

    void collectPastIndiceDailyOhlcvs(Indice indice, LocalDateTime expiredDateTime) {
        // defines
        LocalDateTime dateTimeTo = getIndiceMinDateTime(indice.getIndiceId(), Ohlcv.Type.DAILY)
                .orElse(LocalDateTime.now());
        LocalDateTime dateTimeFrom = dateTimeTo.minusMonths(1);
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
                        .interpolated(ohlcv.isInterpolated())
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("pastIndiceDailyOhlcvEntities[%s]", indice.getIndiceId());
        log.info("PastOhlcvCollector - save {}:{}", unitName, indiceDailyOhlcvEntities.size());
        saveEntities(unitName, indiceDailyOhlcvEntities, transactionManager, indiceOhlcvRepository);
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
