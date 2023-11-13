package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.IndiceOhlcvEntity;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class IndiceOhlcvCollectorTest extends CoreTestSupport {

    private final IndiceCollector indiceOhlcvCollector;

    @Disabled
    @Test
    void collectAssets() throws InterruptedException {
        // given
        // when
        indiceOhlcvCollector.collectIndiceOhlcv();

        // then
        List<IndiceOhlcvEntity> indiceOhlcvEntities = entityManager
                .createQuery("select a from IndiceOhlcvEntity a", IndiceOhlcvEntity.class)
                .setMaxResults(100)
                .getResultList();
        log.info("indiceOhlcvEntities:{}", indiceOhlcvEntities);
        assertTrue(indiceOhlcvEntities.size() > 0);
    }


}