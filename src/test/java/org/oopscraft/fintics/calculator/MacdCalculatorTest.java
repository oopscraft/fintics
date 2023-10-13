package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
class MacdCalculatorTest {

    @Test
    void test() {
        // given
        List<Double> series = new ArrayList<>();
        for(int i = 0; i < 28; i ++) {
           series.add(10000 + (Math.random()*50 - 20));
        }
        int shortTermPeriod = 12;
        int longTermPeriod = 26;
        int signalPeriod = 9;

        // when
        List<Macd> macds = MacdCalculator.of(series, shortTermPeriod, longTermPeriod, signalPeriod)
                .calculate();

        // then
        log.debug("== macds:{}", macds);
    }

}