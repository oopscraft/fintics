package org.oopscraft.fintics.calculator;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class SmaCalculator {

    @Getter
    private final List<Double> smas = new ArrayList<>();

    public static SmaCalculator of(List<Double> series, int period) {
        return new SmaCalculator(series, period);
    }

    public SmaCalculator(List<Double> series, int period) {
        for(int i = 0; i < series.size(); i ++) {
            List<Double> perioidSeries = series.subList(
                Math.max(i - period + 1, 0),
                i + 1
            );

            BigDecimal sum = BigDecimal.ZERO;
            for(double value : perioidSeries) {
                sum = sum.add(BigDecimal.valueOf(value));
            }

            BigDecimal sma = sum.divide(BigDecimal.valueOf(perioidSeries.size()), MathContext.DECIMAL128);
            smas.add(sma.doubleValue());
        }
    }

}
