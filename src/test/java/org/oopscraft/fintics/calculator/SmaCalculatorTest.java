package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SmaCalculatorTest {

    @Test
    @Order(1)
    void test1() {
        // given
        List<BigDecimal> series = new ArrayList<>(){{
            add(BigDecimal.valueOf(100));
            add(BigDecimal.valueOf(200));
            add(BigDecimal.valueOf(300));
        }};

        // when
        List<BigDecimal> smas = SmaCalculator.of(series, 3).calculate();

        // then
        smas.forEach(sma -> log.debug("{}", sma));
    }

}
