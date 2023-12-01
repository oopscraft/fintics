package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.TradeAssetOhlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
class EmaCalculatorTest {

    @Test
    void calculate() {
        // given
        List<TradeAssetOhlcv> series = new ArrayList<>();
        for(int i = 0; i < 500; i ++) {
            series.add(TradeAssetOhlcv.builder()
                    .closePrice(BigDecimal.valueOf(Math.random() * (12000-1000) + 1000))
                    .build());
        }

        // when
        List<Ema> emas = new EmaCalculator(EmaContext.of(10))
                .calculate(series);

        // then
        emas.forEach(ema -> log.debug("{}", ema));
    }


}