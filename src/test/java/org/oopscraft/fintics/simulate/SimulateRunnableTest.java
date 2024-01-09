package org.oopscraft.fintics.simulate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.OrderKind;
import org.oopscraft.fintics.model.Simulate;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class SimulateRunnableTest extends CoreTestSupport {

    private final ApplicationContext applicationContext;

    @Disabled
    @Test
    void run() {
        // given
        Trade trade = Trade.builder()
                .tradeId("test")
                .name("Test Trade")
                .interval(60)
                .threshold(3)
                .startAt(LocalTime.of(9,30,0))
                .endAt(LocalTime.of(15,30,0))
                .holdCondition("log.info('=={}==', dateTime);\nreturn null")
                .orderKind(OrderKind.MARKET)
                .build();
        List<TradeAsset> tradeAssets = new ArrayList<>();
        tradeAssets.add(TradeAsset.builder()
                .tradeId(trade.getTradeId())
                .symbol("122630")
                .name("KODEX 레버리지")
                .enabled(true)
                .holdRatio(BigDecimal.valueOf(30))
                .build());
        trade.setTradeAssets(tradeAssets);

        // when
        Simulate simulate = Simulate.builder()
                .trade(trade)
                .dateTimeFrom(LocalDateTime.now().minusMonths(1))
                .dateTimeTo(LocalDateTime.now())
                .build();
        SimulateRunnable simulateRunnable = SimulateRunnable.builder()
                .simulate(simulate)
                .applicationContext(applicationContext)
                .log(log)
                .build();
        simulateRunnable.run();

        // then
    }

}