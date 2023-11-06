package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
class EmaCalculatorTest {

    @Test
    void calculate() {
        // given
        List<BigDecimal> series = new ArrayList<>();
        for(int i = 0; i < 500; i ++) {
            series.add(BigDecimal.valueOf(Math.random() * (12000-1000) + 1000));
        }

        // when
        Instant start = Instant.now();
        List<BigDecimal> emas = EmaCalculator.of(series, 60).calculate();
        log.info("Duration:{}", Duration.between(start, Instant.now()));

        // then
        emas.forEach(ema -> log.debug("{}", ema));
    }


}