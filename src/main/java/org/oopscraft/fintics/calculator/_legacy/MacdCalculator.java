package org.oopscraft.fintics.calculator._legacy;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MacdCalculator {

    private final List<BigDecimal> series;

    private final int shortPeriod;

    private final int longPeriod;

    private final int signalPeriod;

    private final MathContext mathContext;

    public static MacdCalculator of(List<BigDecimal> series, int shortPeriod, int longPeriod, int signalPeriod) {
        return of(series, shortPeriod, longPeriod, signalPeriod, new MathContext(4, RoundingMode.HALF_UP));
    }

    public static MacdCalculator of(List<BigDecimal> series, int shortPeriod, int longPeriod, int signalPeriod, MathContext mathContext) {
        return new MacdCalculator(series, shortPeriod, longPeriod, signalPeriod, mathContext);
    }

    public MacdCalculator(List<BigDecimal> series, int shortPeriod, int longPeriod, int signalPeriod, MathContext mathContext) {
        this.series = series;
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
        this.signalPeriod = signalPeriod;
        this.mathContext = mathContext;
    }

    public List<Macd> calculate() {
        List<BigDecimal> values = new ArrayList<>();
        List<BigDecimal> oscillators = new ArrayList<>();

        // Calculate MACD line
        List<BigDecimal> shortTermEma = EmaCalculatorLegacy.of(series, shortPeriod, mathContext)
                .calculate();
        List<BigDecimal> longTermEma = EmaCalculatorLegacy.of(series, longPeriod, mathContext)
                .calculate();
        for (int i = 0; i < longTermEma.size(); i++) {
            BigDecimal macd = shortTermEma.get(i).subtract(longTermEma.get(i));
            values.add(macd);
        }

        // ine using MACD line
        List<BigDecimal> signals = new ArrayList<>(EmaCalculatorLegacy.of(values, signalPeriod, mathContext)
                .calculate());

        // oscillator
        for (int i = 0; i < values.size(); i++) {
            BigDecimal oscillator = values.get(i).subtract(signals.get(i));
            oscillators.add(oscillator);
        }

        // return macds
        List<Macd> macds = new ArrayList<>();
        for(int i = 0; i < values.size(); i ++ ) {
            Macd macd = Macd.builder()
                    .value(values.get(i))
                    .signal(signals.get(i))
                    .oscillator(oscillators.get(i))
                    .build();
            macds.add(macd);
        }
        return macds;
    }

}
