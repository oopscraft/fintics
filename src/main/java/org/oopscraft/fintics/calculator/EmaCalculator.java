package org.oopscraft.fintics.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class EmaCalculator {

    private final List<Double> series;

    private final int period;

    public static EmaCalculator of(List<Double> series, int period) {
        return new EmaCalculator(series, period);
    }

    public EmaCalculator(List<Double> series, int period) {
        this.series = series;
        this.period = Math.min(period, series.size());
    }

    public List<Double> calculate() {
        List<Double> emaValues = new ArrayList<>();

        if(series.isEmpty()) {
            return emaValues;
        }

        BigDecimal multiplier = new BigDecimal("2.0")
                .divide(BigDecimal.valueOf(period + 1), 8, RoundingMode.HALF_UP);

        double ema = series.get(0);
        emaValues.add(ema);
        for (int i = 1; i < series.size(); i++) {
            double emaDiff = series.get(i) - ema;
            ema = BigDecimal.valueOf(emaDiff)
                    .multiply(multiplier).doubleValue() + ema;
            emaValues.add(ema);
        }
        return emaValues;
    }

}
