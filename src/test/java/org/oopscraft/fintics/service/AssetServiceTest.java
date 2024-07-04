package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.dao.NewsEntity;
import org.oopscraft.fintics.dao.OhlcvEntity;
import org.oopscraft.fintics.dao.OhlcvSplitEntity;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.News;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.AssetSearch;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

}