package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.model.Trade;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
public class TradeServiceTest extends CoreTestSupport {

    private final TradeService tradeService;

    @Test
    @Order(1)
    void getTrades() {
        // given
        TradeEntity tradeEntity = TradeEntity.builder()
                .tradeId(IdGenerator.uuid())
                .name("test")
                .enabled(true)
                .build();
        entityManager.persist(tradeEntity);

        // when
        List<Trade> trades = tradeService.getTrades();

        // then
        assertTrue(trades.size() > 0);
    }

    @Test
    @Order(1)
    void getTrade() {
        // given
        TradeEntity tradeEntity = TradeEntity.builder()
                .tradeId(IdGenerator.uuid())
                .name("test")
                .enabled(true)
                .build();
        entityManager.persist(tradeEntity);

        // when
        Trade trade = tradeService.getTrade(tradeEntity.getTradeId())
                .orElseThrow();

        // then
        assertEquals(tradeEntity.getTradeId(), trade.getTradeId());
    }

}
