package org.oopscraft.fintics.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MacdCalculator {

    private final List<Double> series;

    private final int shortPeriod;

    private final int longPeriod;

    private final int signalPeriod;

    public static MacdCalculator of(List<Double> series, int shortPeriod, int longPeriod, int signalPeriod) {
        return new MacdCalculator(series, shortPeriod, longPeriod, signalPeriod);
    }

    public MacdCalculator(List<Double> series, int shortTermPeriod, int longTermPeriod, int signalPeriod) {
        this.series = series;
        this.shortPeriod = shortTermPeriod;
        this.longPeriod = longTermPeriod;
        this.signalPeriod = signalPeriod;

    }

    public List<Macd> calculate() {
        List<Double> values = new ArrayList<>();
        List<Double> oscillators = new ArrayList<>();

        // Calculate MACD line
        List<Double> shortTermEMA = EmaCalculator.of(series, shortPeriod)
                .calculate();
        List<Double> longTermEMA = EmaCalculator.of(series, longPeriod)
                .calculate();
        for (int i = 0; i < longTermEMA.size(); i++) {
            double macd = shortTermEMA.get(i) - longTermEMA.get(i);
            values.add(macd);
        }

        // ine using MACD line
        List<Double> signals = new ArrayList<>(EmaCalculator.of(values, signalPeriod)
                .calculate());

        // oscillator
        for (int i = 0; i < values.size(); i++) {
            double oscillator = values.get(i) - signals.get(i);
            oscillators.add(BigDecimal.valueOf(oscillator)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue());
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
