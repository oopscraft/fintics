package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SmaCalculatorTest {

    @Test
    @Order(1)
    void test1() {
        // given
        List<Ohlcv> ohlcvs = new ArrayList<>(){{
            add(Ohlcv.builder().closePrice(BigDecimal.valueOf(100)).build());
            add(Ohlcv.builder().closePrice(BigDecimal.valueOf(200)).build());
            add(Ohlcv.builder().closePrice(BigDecimal.valueOf(300)).build());
        }};

        // when
        List<Sma> smas = new SmaCalculator(SmaContext.of(3)).calculate(ohlcvs);

        // then
        smas.forEach(sma -> log.debug("{}", sma));
    }

}
