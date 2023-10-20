package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
public class SmaCalculatorTest {

    @Test
    @Order(1)
    void test1() {
        // given
        List<Double> series = List.of(
                100.0,
                200.0,
                300.0,
                400.0,
                500.0,
                600.0,
                700.0,
                800.0
        );

        // when
        List<Double> smas = SmaCalculator.of(series, 3).calculate();

        // then
        smas.forEach(sma -> log.debug("{}", sma));
    }

}
