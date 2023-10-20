package org.oopscraft.fintics.calculator;

import lombok.Getter;

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
        this.period = period;
    }

    public List<Double> calculate() {
        List<Double> emas = new ArrayList<>();

        BigDecimal multiplier = BigDecimal.valueOf(2.0)
                .divide(BigDecimal.valueOf(period + 1), 8, RoundingMode.HALF_UP);

        double ema = series.isEmpty() ? 0.0 : series.get(0);
        emas.add(ema);
        for (int i = 1; i < series.size(); i++) {
            double emaDiff = series.get(i) - ema;
            ema = BigDecimal.valueOf(emaDiff)
                    .multiply(multiplier)
                    .doubleValue() + ema;
            emas.add(ema);
        }

        return emas;
    }

}
