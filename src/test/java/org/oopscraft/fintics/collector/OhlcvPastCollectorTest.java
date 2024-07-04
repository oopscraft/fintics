package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.OhlcvEntity;
import org.oopscraft.fintics.model.Asset;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class OhlcvPastCollectorTest extends CoreTestSupport {

    private final OhlcvPastCollector pastOhlcvCollector;

    @PersistenceContext
    private final EntityManager entityManager;

    private final LocalDateTime expiredDatetime = LocalDateTime.now().minusMonths(1);

    @Disabled
    @Test
    void collect() {
        // given
        // when
        pastOhlcvCollector.collect();
        // then
    }

    static Stream<Arguments> getTestAssetArguments() {
        return Stream.of(
                Arguments.of("KR.005930", "XKRX"),      // samsung electronics
                Arguments.of("KR.122630", "XKRX")       // KODEX leverage
        );
    }

    @ParameterizedTest
    @MethodSource("getTestAssetArguments")
    void collectPastMinuteOhlcvs(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        // when
        pastOhlcvCollector.collectPastMinuteOhlcvs(asset, expiredDatetime);
        // then
        List<OhlcvEntity> ohlcvEntities = entityManager.createQuery("select " +
                                " a from OhlcvEntity a " +
                                " where a.assetId = :assetId" +
                                " and a.type = 'MINUTE'" +
                                "order by a.dateTime desc",
                        OhlcvEntity.class)
                .setParameter("assetId", asset.getAssetId())
                .getResultList();
        log.info("ohlcvEntities.size:{}", ohlcvEntities.size());
        assertTrue(ohlcvEntities.size() > 0);
    }

    @Disabled
    @Test
    void collectPastDailyOhlcvs() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .exchange("XKRX")
                .build();
        // when
        pastOhlcvCollector.collectPastDailyOhlcvs(asset, expiredDatetime);
        // then
        List<OhlcvEntity> ohlcvEntities = entityManager.createQuery("select " +
                                " a from OhlcvEntity a " +
                                " where a.assetId = :assetId" +
                                " and a.type = 'DAILY'" +
                                " order by a.dateTime desc",
                        OhlcvEntity.class)
                .setParameter("assetId", asset.getAssetId())
                .getResultList();
        log.info("ohlcvEntities.size:{}", ohlcvEntities.size());
        assertTrue(ohlcvEntities.size() > 0);
    }

}