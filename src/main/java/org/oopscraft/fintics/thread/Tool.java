package org.oopscraft.fintics.thread;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tool {

    public BigDecimal slope(List<BigDecimal> values, int period) {
        List<BigDecimal> periodValues = values.subList(
                0,
                Math.min(period, values.size())
        );
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
        return sum.divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL128)
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
        return sum.divide(BigDecimal.valueOf(period), MathContext.DECIMAL128)
                .setScale(2, RoundingMode.HALF_UP);
    }

}
