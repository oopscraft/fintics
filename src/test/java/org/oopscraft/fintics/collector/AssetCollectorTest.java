package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.AssetEntity;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class AssetCollectorTest extends CoreTestSupport {

    private final AssetCollector assetCollector;

    @Disabled
    @Test
    void collectAssets() {
        // given
        // when
        assetCollector.collectAssets();

        // then
        List<AssetEntity> assetEntities = entityManager.createQuery("select a from AssetEntity a", AssetEntity.class)
                .setMaxResults(100)
                .getResultList();
        log.info("assetEntities:{}", assetEntities);
        assertTrue(assetEntities.size() > 0);
    }


}