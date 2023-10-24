package org.oopscraft.fintics.thread;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tool {

    public Double slope(List<Double> values, int period) {
        List<Double> periodValues = values.subList(
                0,
                Math.min(period, values.size())
        );
        List<Double> series = new ArrayList<>(periodValues);
        Collections.reverse(series);

        // check empty
        if(series.isEmpty()) {
            return 0.0;
        }

        // sum
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < series.size(); i++) {
            BigDecimal change = BigDecimal.valueOf(series.get(i))
                    .subtract(BigDecimal.valueOf(series.get(Math.max(i-1,0))));
            sum = sum.add(change);
        }

        // average
        return sum.divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL128)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public Double average(List<Double> values, int period) {
        if(values.isEmpty()) {
            return BigDecimal.ZERO.doubleValue();
        }
        if(values.size() < period) {
            period = values.size();
        }
        BigDecimal sum = BigDecimal.ZERO;
        for(int i = 0; i < period; i ++ ) {
            Double value = values.get(i);
            sum = sum.add(BigDecimal.valueOf(value));
        }
        return sum.divide(BigDecimal.valueOf(period), MathContext.DECIMAL128)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

}
