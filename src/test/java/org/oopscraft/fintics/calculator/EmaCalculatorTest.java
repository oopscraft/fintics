package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
class EmaCalculatorTest {

    @Test
    void calculate() {
        // given
        List<BigDecimal> series = new ArrayList<>(){{
            add(BigDecimal.valueOf(100));
            add(BigDecimal.valueOf(200));
        }};

        // when
        List<BigDecimal> emas = EmaCalculator.of(series, 3).calculate();

        // then
        emas.forEach(ema -> log.debug("{}", ema));
    }


}