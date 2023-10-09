package org.oopscraft.fintics.calculator;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class MacdCalculatorTest {

    @Test
    @Order(1)
    void calculator() {
        List<Double> prices = new ArrayList<>();
        // Add historical closing prices here
        prices.add(1000.0);
        prices.add(900.0);
        prices.add(1000.0);
        prices.add(1100.0);

        MacdCalculator macdCalculator = new MacdCalculator(prices);
        List<Double> macdHistogram = macdCalculator.calculateMacd(12, 26, 9);

        System.out.println("MACD Histogram:");
        for (Double value : macdHistogram) {
            System.out.println(value);
        }
    }

}
