package org.oopscraft.fintics.calculator;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class MacdCalculatorTest {

    @Test
    void test() {
        List<Double> prices = new ArrayList<>();
        for(int i = 0; i < 28; i ++) {
           prices.add(10000 + (Math.random()*50 - 20));
        }

        int shortTermPeriod = 12;
        int longTermPeriod = 26;
        int signalPeriod = 9;

        List<BigDecimal> macdHistograms = MacdCalculator.calculate(
                prices.stream()
                        .map(BigDecimal::valueOf)
                        .collect(Collectors.toList()),
                shortTermPeriod,
                longTermPeriod,
                signalPeriod);

        System.out.println("MACD Values:");
        for (BigDecimal macd : macdHistograms) {
            System.out.println(macd);
        }


    }

}