package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.BrokerAssetEntity;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.model.Trade;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
class BrokerAssetCollectorTest extends CoreTestSupport {

    private final BrokerAssetCollector brokerAssetCollector;

    @PersistenceContext
    private final EntityManager entityManager;

    @Disabled
    @Test
    void collect() {
        // given
        TradeEntity tradeEntity = TradeEntity.builder()
                .tradeId("test")
                .brokerId("KIS")
                .brokerConfig("test=test")
                .enabled(true)
                .build();
        entityManager.persist(tradeEntity);
        entityManager.flush();

        // when
        brokerAssetCollector.collect();

        // then
        List<BrokerAssetEntity> brokerAssetEntities = entityManager
                .createQuery("select a from BrokerAssetEntity a where a.brokerId = :brokerId", BrokerAssetEntity.class)
                .setParameter("brokerId", tradeEntity.getBrokerId())
                .getResultList();
        assertTrue(brokerAssetEntities.size() > 0);
    }

    @Disabled
    @Test
    void saveBrokerAssets() {
        // given
        Trade trade = Trade.builder()
                .brokerId("KIS")
                .brokerConfig("test=test")
                .build();
        // when
        brokerAssetCollector.saveBrokerAssets(trade);
        // then
        List<BrokerAssetEntity> brokerAssetEntities = entityManager
                .createQuery("select a from BrokerAssetEntity a where a.brokerId = :brokerId", BrokerAssetEntity.class)
                .setParameter("brokerId", trade.getBrokerId())
                .getResultList();
        assertTrue(brokerAssetEntities.size() > 0);
    }

}