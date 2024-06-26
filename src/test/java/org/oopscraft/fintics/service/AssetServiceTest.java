package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.dao.AssetNewsEntity;
import org.oopscraft.fintics.dao.AssetOhlcvEntity;
import org.oopscraft.fintics.dao.AssetOhlcvSplitEntity;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetSearch;
import org.oopscraft.fintics.model.News;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        AssetSearch assetSearch = AssetSearch.builder()
                .assetId(assetId)
                .build();
        Page<Asset> assetPage = assetService.getAssets(assetSearch, PageRequest.of(0, 10));
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
    void getAssetDailyOhlcvWithForwardSplit() {
        // given
        String assetId = "test";
        // 액면 분할 정보
        entityManager.persist(AssetOhlcvSplitEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDateTime.of(2011,3,16, 0, 0, 0))
                .splitFrom(BigDecimal.valueOf(1))
                .splitTo(BigDecimal.valueOf(10))
                .build());
        // 액면 분할 전 가격 정보
        entityManager.persist(AssetOhlcvEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDate.of(2011, 3, 15).atStartOfDay())
                .type(Ohlcv.Type.DAILY)
                .openPrice(BigDecimal.valueOf(1000))
                .highPrice(BigDecimal.valueOf(1010))
                .lowPrice(BigDecimal.valueOf(990))
                .closePrice(BigDecimal.valueOf(1000))
                .volume(BigDecimal.valueOf(123))
                .build());
        // 액면 분할 후 가격 정보
        entityManager.persist(AssetOhlcvEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDate.of(2011, 3, 16).atStartOfDay())
                .type(Ohlcv.Type.DAILY)
                .openPrice(BigDecimal.valueOf(100))
                .highPrice(BigDecimal.valueOf(101))
                .lowPrice(BigDecimal.valueOf(99))
                .closePrice(BigDecimal.valueOf(100))
                .volume(BigDecimal.valueOf(1230))
                .build());
        entityManager.flush();
        // when
        List<Ohlcv> assetDailyOhlcvs = assetService.getAssetDailyOhlcvs(
                assetId,
                LocalDateTime.of(2011,1,1, 0, 0, 0),
                LocalDateTime.of(2011,12,31, 23, 59, 59),
                Pageable.unpaged()
        );
        // then
        log.info("assetDailyOhlcvs: {}", assetDailyOhlcvs);
        // 액면 분할 후 가격은 그대로 보존 (100)
        assertEquals(100, assetDailyOhlcvs.get(0).getClosePrice().doubleValue());
        // 액면 분할 전 가격은 액면 분할 비율이 적용된 가격 (1000 -> 100)
        assertEquals(100, assetDailyOhlcvs.get(1).getClosePrice().doubleValue());
    }

    @Test
    void getAssetDailyOhlcvWithReverseSplit() {
        // given
        String assetId = "test";
        // 액면 병합 정보
        entityManager.persist(AssetOhlcvSplitEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDateTime.of(2011,3,16, 0, 0, 0))
                .splitFrom(BigDecimal.valueOf(10))
                .splitTo(BigDecimal.valueOf(1))
                .build());
        // 액면 병합 전 가격 정보
        entityManager.persist(AssetOhlcvEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDate.of(2011, 3, 15).atStartOfDay())
                .type(Ohlcv.Type.DAILY)
                .openPrice(BigDecimal.valueOf(100))
                .highPrice(BigDecimal.valueOf(101))
                .lowPrice(BigDecimal.valueOf(99))
                .closePrice(BigDecimal.valueOf(100))
                .volume(BigDecimal.valueOf(1230))
                .build());
        // 액면 병합 후 가격 정보
        entityManager.persist(AssetOhlcvEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDate.of(2011, 3, 16).atStartOfDay())
                .type(Ohlcv.Type.DAILY)
                .openPrice(BigDecimal.valueOf(1000))
                .highPrice(BigDecimal.valueOf(1010))
                .lowPrice(BigDecimal.valueOf(990))
                .closePrice(BigDecimal.valueOf(1000))
                .volume(BigDecimal.valueOf(123))
                .build());
        entityManager.flush();
        // when
        List<Ohlcv> assetDailyOhlcvs = assetService.getAssetDailyOhlcvs(
                assetId,
                LocalDateTime.of(2011,1,1, 0, 0, 0),
                LocalDateTime.of(2011,12,31, 23, 59, 59),
                Pageable.unpaged()
        );
        // then
        log.info("assetDailyOhlcvs: {}", assetDailyOhlcvs);
        // 액면 병합 후 가격은 그대로 보존 (1000)
        assertEquals(1000, assetDailyOhlcvs.get(0).getClosePrice().doubleValue());
        // 액면 병합 전 가격은 액면 분할 비율이 적용된 가격 (1000 -> 10)
        assertEquals(10, assetDailyOhlcvs.get(1).getClosePrice().doubleValue());
    }


    @Test
    void getAssetDailyOhlcvWithAVGOSplit() {
        // given
        String assetId = "US.AVGO";
        // 브로드컴(AVGO) 2024-07-15 10->1 액면 분할
        entityManager.persist(AssetOhlcvSplitEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDateTime.of(2024,7,15, 0, 0, 0))
                .splitFrom(BigDecimal.valueOf(1))
                .splitTo(BigDecimal.valueOf(10))
                .build());
        // 액면 분할 전 가격 정보
        entityManager.persist(AssetOhlcvEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDate.of(2024, 7, 14).atStartOfDay())
                .type(Ohlcv.Type.DAILY)
                .openPrice(BigDecimal.valueOf(1606.34))
                .highPrice(BigDecimal.valueOf(1614.28))
                .lowPrice(BigDecimal.valueOf(1598.66))
                .closePrice(BigDecimal.valueOf(1599.23))
                .volume(BigDecimal.valueOf(2946))
                .build());
        // 액면 분할 후 가격 정보
        entityManager.persist(AssetOhlcvEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDate.of(2024, 7, 15).atStartOfDay())
                .type(Ohlcv.Type.DAILY)
                .openPrice(BigDecimal.valueOf(160.634))
                .highPrice(BigDecimal.valueOf(161.428))
                .lowPrice(BigDecimal.valueOf(159.866))
                .closePrice(BigDecimal.valueOf(159.923))
                .volume(BigDecimal.valueOf(29460))
                .build());
        entityManager.flush();
        // when
        List<Ohlcv> assetDailyOhlcvs = assetService.getAssetDailyOhlcvs(
                assetId,
                LocalDateTime.of(2024,1,1, 0, 0, 0),
                LocalDateTime.of(2024,12,31, 23, 59, 59),
                Pageable.unpaged()
        );
        // then
        log.info("assetDailyOhlcvs: {}", assetDailyOhlcvs);
        // 액면 분할 후 가격은 그대로 보존 (1000)
        assertEquals(159.923, assetDailyOhlcvs.get(0).getClosePrice().doubleValue());
        // 액면 분할 전 가격은 액면 분할 비율이 적용된 가격 (1000 -> 10)
        assertEquals(159.923, assetDailyOhlcvs.get(1).getClosePrice().doubleValue());
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