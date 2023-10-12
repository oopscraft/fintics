package org.oopscraft.fintics.calculator;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class RsiCalculator {

    public static List<BigDecimal> calculate(List<BigDecimal> prices, int period) {
        List<BigDecimal> rsiValues = new ArrayList<>();

        List<BigDecimal> priceChanges = new ArrayList<>();
        for (int i = 0; i < prices.size(); i++) {
            BigDecimal priceChange = prices.get(i).subtract(prices.get(Math.max(i-1,0)));
            priceChanges.add(priceChange);
        }

        List<BigDecimal> gainValues = new ArrayList<>();
        List<BigDecimal> lossValues = new ArrayList<>();

        for (int i = 0; i < priceChanges.size(); i++) {
            if (priceChanges.get(i).compareTo(BigDecimal.ZERO) > 0) {
                gainValues.add(priceChanges.get(i));
                lossValues.add(BigDecimal.ZERO);
            } else {
                gainValues.add(BigDecimal.ZERO);
                lossValues.add(priceChanges.get(i).abs());
            }
        }

        BigDecimal avgGain = BigDecimal.valueOf(
                new Mean().evaluate(gainValues.stream()
                        .mapToDouble(BigDecimal::doubleValue)
                        .toArray()));
        BigDecimal avgLoss = BigDecimal.valueOf(
                new Mean().evaluate(lossValues.stream()
                        .mapToDouble(BigDecimal::doubleValue)
                        .toArray()));

        for (int i = 0; i < prices.size(); i++) {
            BigDecimal relativeStrength = avgGain.compareTo(BigDecimal.ZERO) != 0 ? avgGain.divide(avgLoss, MathContext.DECIMAL128) : BigDecimal.ZERO;
            BigDecimal rs = relativeStrength.add(BigDecimal.ONE, MathContext.DECIMAL128);
            BigDecimal rsi = BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(100).divide(rs, MathContext.DECIMAL128));
            rsiValues.add(rsi);

            BigDecimal currentGain = priceChanges.get(i).compareTo(BigDecimal.ZERO) > 0 ? priceChanges.get(i) : BigDecimal.ZERO;
            BigDecimal currentLoss = priceChanges.get(i).compareTo(BigDecimal.ZERO) < 0 ? priceChanges.get(i).abs() : BigDecimal.ZERO;

            avgGain = avgGain.multiply(BigDecimal.valueOf(period - 1), MathContext.DECIMAL128)
                    .add(currentGain, MathContext.DECIMAL128)
                    .divide(BigDecimal.valueOf(period), MathContext.DECIMAL128);

            avgLoss = avgLoss.multiply(BigDecimal.valueOf(period - 1), MathContext.DECIMAL128)
                    .add(currentLoss, MathContext.DECIMAL128)
                    .divide(BigDecimal.valueOf(period), MathContext.DECIMAL128);
        }

        return rsiValues;
    }

}
