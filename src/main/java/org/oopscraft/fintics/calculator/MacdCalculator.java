package org.oopscraft.fintics.calculator;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MacdCalculator {

    private final List<Double> series;

    private final int shortTermPeriod;

    private final int longTermPeriod;

    private final int signalPeriod;

    public static MacdCalculator of(List<Double> series, int shortTermPeriod, int longTermPeriod, int signalPeriod) {
        return new MacdCalculator(series, shortTermPeriod, longTermPeriod, signalPeriod);
    }

    public MacdCalculator(List<Double> series, int shortTermPeriod, int longTermPeriod, int signalPeriod) {
        this.series = series;
        this.shortTermPeriod = shortTermPeriod;
        this.longTermPeriod = longTermPeriod;
        this.signalPeriod = signalPeriod;
    }

    public List<Macd> calculate() {
        // Calculate MACD line
        List<Double> shortTermEMA = EmaCalculator.of(series, shortTermPeriod)
                .calculate();
        List<Double> longTermEMA = EmaCalculator.of(series, longTermPeriod)
                .calculate();
        List<Double> macdValues = new ArrayList<>();
        for (int i = 0; i < longTermEMA.size(); i++) {
            double macd = shortTermEMA.get(i) - longTermEMA.get(i);
            macdValues.add(macd);
        }

        // ine using MACD line
        List<Double> signalValues = EmaCalculator.of(macdValues, signalPeriod)
                .calculate();

        // oscillator
        List<Double> oscillatorValues = new ArrayList<>();
        for (int i = 0; i < macdValues.size(); i++) {
            double oscillator = macdValues.get(i) - signalValues.get(i);
            oscillatorValues.add(oscillator);
        }

        // result
        List<Macd> macds = new ArrayList<>();
        for(int i = 0, size = this.series.size(); i < size; i ++ ) {
            Macd macd = Macd.builder()
                    .series(setScale(series.get(i)))
                    .macd(setScale(macdValues.get(i)))
                    .signal(setScale(signalValues.get(i)))
                    .oscillator(setScale(oscillatorValues.get(i)))
                    .build();
            macds.add(macd);
        }

        return macds;
    }

    private double setScale(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

}
