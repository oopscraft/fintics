package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.broker.kis.KisBrokerClient;
import org.oopscraft.fintics.client.broker.kis.KisBrokerClientDefinition;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.dao.BrokerEntity;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.model.Trade;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
class AssetCollectorTest extends CoreTestSupport {

    private final AssetCollector assetCollector;

    @PersistenceContext
    private final EntityManager entityManager;

    @Disabled
    @Test
    void collect() {
        // given
        TradeEntity tradeEntity = TradeEntity.builder()
                .tradeId("test")
                .enabled(true)
                .build();
        entityManager.persist(tradeEntity);
        entityManager.flush();

        // when
        assetCollector.collect();

        // then
        List<AssetEntity> brokerAssetEntities = entityManager
                .createQuery("select a from AssetEntity a", AssetEntity.class)
                .getResultList();
        assertTrue(brokerAssetEntities.size() > 0);
    }

    @Disabled
    @Test
    void saveAssets() {
        // given
        entityManager.persist(BrokerEntity.builder()
                .brokerId("test")
                .brokerClientId("KIS")
                .build());
        Trade trade = Trade.builder()
                .tradeId("test")
                .brokerId("test")
                .build();
        // when
        assetCollector.saveAssets(trade);
        // then
        List<AssetEntity> brokerAssetEntities = entityManager
                .createQuery("select a from AssetEntity a", AssetEntity.class)
                .getResultList();
        assertTrue(brokerAssetEntities.size() > 0);
    }

}