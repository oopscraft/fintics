package org.oopscraft.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.AssetOhlcvEntity;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class AssetOhlcvCollectorTest extends CoreTestSupport {

    private final AssetOhlcvCollector assetOhlcvCollector;

    @Disabled
    @Test
    void collectAssets() throws InterruptedException {
        // given
        // when
        assetOhlcvCollector.collect();

        // then
        List<AssetOhlcvEntity> assetOhlcvEntities = entityManager
                .createQuery("select a from AssetOhlcvEntity a", AssetOhlcvEntity.class)
                .setMaxResults(100)
                .getResultList();
        log.info("assetOhlcvEntities:{}", assetOhlcvEntities);
        assertTrue(assetOhlcvEntities.size() > 0);
    }


}