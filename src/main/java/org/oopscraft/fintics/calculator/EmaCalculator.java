package org.oopscraft.fintics.calculator;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class EmaCalculator {

    public static List<BigDecimal> calculate(List<BigDecimal> prices, int period) {
        List<BigDecimal> emaValues = new ArrayList<>();

        if (prices.size() < period) {
            period = prices.size();
        }

        BigDecimal multiplier = new BigDecimal("2.0")
                .divide(new BigDecimal(period + 1), 10, RoundingMode.HALF_UP);

        BigDecimal ema = BigDecimal.valueOf(prices.get(0).doubleValue());
        emaValues.add(ema);
        for (int i = 1; i < prices.size(); i++) {
            BigDecimal emaDiff = prices.get(i).subtract(ema);
            ema = emaDiff.multiply(multiplier).add(ema);
            emaValues.add(ema);
        }
        return emaValues;
    }

}
