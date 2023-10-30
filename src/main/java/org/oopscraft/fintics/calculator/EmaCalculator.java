package org.oopscraft.fintics.calculator;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class EmaCalculator {

    private final List<BigDecimal> series;

    private final int period;

    public static EmaCalculator of(List<BigDecimal> series, int period) {
        return new EmaCalculator(series, period);
    }

    public EmaCalculator(List<BigDecimal> series, int period) {
        this.series = series;
        this.period = period;
    }

    public List<BigDecimal> calculate() {
        List<BigDecimal> emas = new ArrayList<>();

        BigDecimal multiplier = BigDecimal.valueOf(2.0)
                .divide(BigDecimal.valueOf(period + 1), MathContext.DECIMAL128);

        BigDecimal ema = series.isEmpty() ? BigDecimal.ZERO : series.get(0);
        emas.add(ema);
        for (int i = 1; i < series.size(); i++) {
            BigDecimal emaDiff = series.get(i).subtract(ema);
            ema = emaDiff
                    .multiply(multiplier)
                    .add(ema)
                    .setScale(2, RoundingMode.HALF_UP);
            emas.add(ema);
        }

        return emas;
    }

}
