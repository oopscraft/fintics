package org.oopscraft.fintics.thread;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Tool {

    public BigDecimal zScore(List<BigDecimal> values, int period) {
        if(values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> periodValues = values.subList(0, Math.min(period, values.size()));
        List<BigDecimal> series = new ArrayList<>(periodValues);
        Collections.reverse(series);

        BigDecimal mean = series.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL32);

        BigDecimal sumSquaredDeviations = series.stream()
                .map(x -> x.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal variance = sumSquaredDeviations
                .divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL32);
        BigDecimal standardDeviation = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));

        List<BigDecimal> zScores =  series.stream()
                .map(x -> x.subtract(mean)
                        .divide(standardDeviation, MathContext.DECIMAL32))
                .toList();

        return zScores.get(Math.max(zScores.size()-1,0));
    }

    public BigDecimal slope(List<BigDecimal> values, int period) {
        List<BigDecimal> periodValues = values.subList(0, Math.min(period, values.size()));
        List<BigDecimal> series = new ArrayList<>(periodValues);
        Collections.reverse(series);

        // check empty
        if(series.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // sum
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < series.size(); i++) {
            BigDecimal change = series.get(i)
                    .subtract(series.get(Math.max(i-1,0)));
            sum = sum.add(change);
        }

        // average
        return sum.divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL32)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal average(List<BigDecimal> values, int period) {
        if(values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        if(values.size() < period) {
            period = values.size();
        }
        BigDecimal sum = BigDecimal.ZERO;
        for(int i = 0; i < period; i ++ ) {
            sum = sum.add(values.get(i));
        }
        return sum.divide(BigDecimal.valueOf(period), MathContext.DECIMAL32)
                .setScale(2, RoundingMode.HALF_UP);
    }

}
