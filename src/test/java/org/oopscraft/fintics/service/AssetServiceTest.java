package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.News;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class AssetServiceTest extends CoreTestSupport {

    private final AssetService assetService;

    @PersistenceContext
    private final EntityManager entityManager;

    @Test
    void getAssets() {
        // given
        String assetId = "test";
        String assetName = "test name";
        entityManager.persist(AssetEntity.builder()
                .assetId(assetId)
                .assetName(assetName)
                .build());
        entityManager.flush();
        // when
        Page<Asset> assetPage = assetService.getAssets(assetId, null, null, PageRequest.of(0, 10));
        // then
        assertTrue(assetPage.getContent().stream().anyMatch(it -> Objects.equals(it.getAssetId(), assetId)));
        assertEquals(assetId, assetPage.getContent().get(0).getAssetId());
        assertEquals(assetName, assetPage.getContent().get(0).getAssetName());
    }

    @Test
    void getAsset() {
        // given
        String assetId = "test";
        String assetName = "test name";
        entityManager.persist(AssetEntity.builder()
                .assetId(assetId)
                .assetName(assetName)
                .build());
        entityManager.flush();
        // when
        Asset asset = assetService.getAsset(assetId).orElseThrow();
        // then
        assertEquals(asset.getAssetId(), assetId);
    }

    @Test
    void getAssetDailyOhlcvs() {
        String assetId = "test";
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusMonths(1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime dateTimeTo = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        List.of(dateTimeFrom, dateTimeTo).forEach(dateTime -> {
            entityManager.persist(AssetOhlcvEntity.builder()
                    .assetId(assetId)
                    .type(Ohlcv.Type.DAILY)
                    .dateTime(dateTime)
                    .openPrice(BigDecimal.ONE)
                    .highPrice(BigDecimal.ONE)
                    .lowPrice(BigDecimal.ONE)
                    .closePrice(BigDecimal.ONE)
                    .volume(BigDecimal.ONE)
                    .build());
        });
        entityManager.flush();
        // when
        List<Ohlcv> assetOhlcvs = assetService.getAssetDailyOhlcvs(assetId, dateTimeFrom, dateTimeTo, PageRequest.of(0, 10));
        // then
        assertEquals(2, assetOhlcvs.size());
    }

    @Test
    void getAssetMinuteOhlcvs() {
        String assetId = "test";
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime dateTimeTo = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List.of(dateTimeFrom, dateTimeTo).forEach(dateTime -> {
            entityManager.persist(AssetOhlcvEntity.builder()
                    .assetId(assetId)
                    .type(Ohlcv.Type.MINUTE)
                    .dateTime(dateTime)
                    .openPrice(BigDecimal.ONE)
                    .highPrice(BigDecimal.ONE)
                    .lowPrice(BigDecimal.ONE)
                    .closePrice(BigDecimal.ONE)
                    .volume(BigDecimal.ONE)
                    .build());
        });
        entityManager.flush();
        // when
        List<Ohlcv> assetOhlcvs = assetService.getAssetMinuteOhlcvs(assetId, dateTimeFrom, dateTimeTo, PageRequest.of(0, 10));
        // then
        assertEquals(2, assetOhlcvs.size());
    }

    @Test
    void getAssetNewses() {
        // given
        String assetId = "test";
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime dateTimeTo = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List.of(dateTimeFrom, dateTimeTo).forEach(dateTime -> {
            entityManager.persist(AssetNewsEntity.builder()
                    .assetId(assetId)
                    .dateTime(dateTime)
                    .newsId(UUID.randomUUID().toString().replaceAll("-",""))
                    .build());
        });
        entityManager.flush();
        // when
        List<News> assetNewses = assetService.getAssetNewses(assetId, dateTimeFrom, dateTimeTo, PageRequest.of(0, 10));
        // then
        assertTrue(assetNewses.size() > 0);
    }


}