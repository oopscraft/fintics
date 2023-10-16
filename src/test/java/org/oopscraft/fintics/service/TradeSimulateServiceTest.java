package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.SimulateTrade;
import org.oopscraft.fintics.model.Trade;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
public class TradeSimulateServiceTest extends CoreTestSupport {

    private static final String TRADE_ID = "06c228451ce0400fa57bb36f0568d7cb";

    private final TradeService tradeService;

    private final SimulateTradeService simulateTradeService;

    @Disabled
    @Test
    @Order(1)
    void simulate() {
        // given
        Trade trade = tradeService.getTrade(TRADE_ID).orElseThrow();

        // when
        SimulateTrade simulateTrade = simulateTradeService.simulateTrade(trade);

        // then
        assertNotNull(simulateTrade);
    }

}
