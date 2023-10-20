package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
class EmaCalculatorTest {

    @Test
    void calculate() {
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
        List<Double> emas = EmaCalculator.of(series, 3).calculate();

        // then
        emas.forEach(ema -> log.debug("{}", ema));
    }


}