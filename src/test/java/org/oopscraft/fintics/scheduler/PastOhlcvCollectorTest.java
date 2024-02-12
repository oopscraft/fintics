package org.oopscraft.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.AssetOhlcvEntity;
import org.oopscraft.fintics.dao.IndiceOhlcvEntity;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.IndiceId;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class PastOhlcvCollectorTest extends CoreTestSupport {

    private final PastOhlcvCollector pastOhlcvCollector;

    @PersistenceContext
    private final EntityManager entityManager;

    private final LocalDateTime expiredDateTime = LocalDateTime.now().minusMonths(1);

    @Disabled
    @Test
    void collect() {
        // given
        // when
        pastOhlcvCollector.collect();
        // then
    }

    @Disabled
    @Test
    void getOhlcvs() {
        // given
        String yahooSymbol = "005930.KS";
        Ohlcv.Type type = Ohlcv.Type.MINUTE;
        LocalDateTime dateTimeTo = LocalDateTime.now().minusDays(1);
        LocalDateTime dateTimeFrom = dateTimeTo.minusWeeks(11);
        // when
        List<Ohlcv> ohlcvs = pastOhlcvCollector.getOhlcvs(yahooSymbol, type, dateTimeFrom, dateTimeTo);
        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() > 0);
    }

    @Disabled
    @Test
    void collectPastAssetMinuteOhlcvs() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .exchange("KRX")
                .build();
        // when
        pastOhlcvCollector.collectPastAssetMinuteOhlcvs(asset, expiredDateTime);
        // then
        List<AssetOhlcvEntity> assetOhlcvEntities = entityManager.createQuery("select " +
                                " a from AssetOhlcvEntity a " +
                                " where a.assetId = :assetId" +
                                " and a.type = 'MINUTE'" +
                                "order by a.dateTime desc",
                        AssetOhlcvEntity.class)
                .setParameter("assetId", asset.getAssetId())
                .getResultList();
        log.info("assetOhlcvEntities.size:{}", assetOhlcvEntities.size());
        assertTrue(assetOhlcvEntities.size() > 0);
    }

    @Disabled
    @Test
    void collectPastAssetDailyOhlcvs() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .exchange("KRX")
                .build();
        // when
        pastOhlcvCollector.collectPastAssetDailyOhlcvs(asset, expiredDateTime);
        // then
        List<AssetOhlcvEntity> assetOhlcvEntities = entityManager.createQuery("select " +
                                " a from AssetOhlcvEntity a " +
                                " where a.assetId = :assetId" +
                                " and a.type = 'DAILY'" +
                                " order by a.dateTime desc",
                        AssetOhlcvEntity.class)
                .setParameter("assetId", asset.getAssetId())
                .getResultList();
        log.info("assetOhlcvEntities.size:{}", assetOhlcvEntities.size());
        assertTrue(assetOhlcvEntities.size() > 0);
    }

    @Disabled
    @Test
    void collectPastIndiceMinuteOhlcvs() {
        // given
        IndiceId indiceId = IndiceId.NDX_FUTURE;
        // when
        pastOhlcvCollector.collectPastIndiceMinuteOhlcvs(indiceId, expiredDateTime);
        // then
        List<IndiceOhlcvEntity> indiceOhlcvEntities = entityManager.createQuery("select " +
                                " a from IndiceOhlcvEntity a " +
                                " where a.indiceId = :indiceId" +
                                " and a.type = 'MINUTE'" +
                                "order by a.dateTime desc",
                        IndiceOhlcvEntity.class)
                .setParameter("indiceId", indiceId)
                .getResultList();
        log.info("indiceOhlcvEntities.size:{}", indiceOhlcvEntities.size());
        assertTrue(indiceOhlcvEntities.size() > 0);
    }

    @Disabled
    @Test
    void collectPastIndiceDailyOhlcvs() {
        // given
        IndiceId indiceId = IndiceId.NDX_FUTURE;
        // when
        pastOhlcvCollector.collectPastIndiceDailyOhlcvs(indiceId, expiredDateTime);
        // then
        List<IndiceOhlcvEntity> indiceOhlcvEntities = entityManager.createQuery("select " +
                                " a from IndiceOhlcvEntity a " +
                                " where a.indiceId = :indiceId" +
                                " and a.type = 'DAILY'" +
                                " order by a.dateTime desc",
                        IndiceOhlcvEntity.class)
                .setParameter("indiceId", indiceId)
                .getResultList();
        log.info("indiceOhlcvEntities.size:{}", indiceOhlcvEntities.size());
        assertTrue(indiceOhlcvEntities.size() > 0);
    }

}