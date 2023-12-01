package org.oopscraft.fintics.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.TradeAssetOhlcvEntity;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class TradeCollectorTest extends CoreTestSupport {

    private final TradeCollector tradeCollector;

    @Disabled
    @Test
    void collectAssets() throws InterruptedException {
        // given
        // when
        tradeCollector.collectTradeAssetOhlcv();

        // then
        List<TradeAssetOhlcvEntity> tradeAssetOhlcvEntities = entityManager
                .createQuery("select a from TradeAssetOhlcvEntity a", TradeAssetOhlcvEntity.class)
                .setMaxResults(100)
                .getResultList();
        log.info("tradeAssetOhlcvEntities:{}", tradeAssetOhlcvEntities);
        assertTrue(tradeAssetOhlcvEntities.size() > 0);
    }


}