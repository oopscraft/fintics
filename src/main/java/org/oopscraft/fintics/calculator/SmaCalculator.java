package org.oopscraft.fintics.calculator;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class SmaCalculator {

    private final List<BigDecimal> series;

    private final int period;

    public static SmaCalculator of(List<BigDecimal> series, int period) {
        return new SmaCalculator(series, period);
    }

    public SmaCalculator(List<BigDecimal> series, int period) {
        this.series = series;
        this.period = period;
    }

    public List<BigDecimal> calculate() {
        List<BigDecimal> smas = new ArrayList<>();
        for(int i = 0; i < series.size(); i ++) {
            List<BigDecimal> perioidSeries = series.subList(
                Math.max(i - period + 1, 0),
                i + 1
            );

            BigDecimal sum = BigDecimal.ZERO;
            for(BigDecimal value : perioidSeries) {
                sum = sum.add(value);
            }

            BigDecimal sma = sum.divide(BigDecimal.valueOf(perioidSeries.size()), MathContext.DECIMAL128)
                    .setScale(2, RoundingMode.HALF_UP);
            smas.add(sma);
        }

        return smas;
    }

}
