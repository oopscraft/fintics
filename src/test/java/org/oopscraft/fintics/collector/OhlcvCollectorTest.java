package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.OhlcvEntity;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class AssetOhlcvCollectorTest extends CoreTestSupport {

    private final OhlcvCollector assetOhlcvCollector;

    @Disabled
    @Test
    void collect() {
        // given
        // when
        assetOhlcvCollector.collect();

        // then
        List<OhlcvEntity> assetOhlcvEntities = entityManager
                .createQuery("select a from OhlcvEntity a", OhlcvEntity.class)
                .setMaxResults(100)
                .getResultList();
        log.info("assetOhlcvEntities:{}", assetOhlcvEntities);
        assertTrue(assetOhlcvEntities.size() > 0);
    }


}