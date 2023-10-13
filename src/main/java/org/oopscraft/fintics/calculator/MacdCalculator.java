package org.oopscraft.fintics.calculator;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

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
            Mean mean = new Mean();
            double periodAverageMacd = mean.evaluate(macdValues
                    .subList(Math.max(i-signalPeriod,0), i+1).stream()
                            .mapToDouble(Double::doubleValue)
                            .toArray());
            double oscillator = macdValues.get(i) - periodAverageMacd;
            oscillatorValues.add(oscillator);
        }

        // result
        List<Macd> macds = new ArrayList<>();
        for(int i = 0, size = this.series.size(); i < size; i ++ ) {
            Macd macd = Macd.builder()
                    .series(series.get(i))
                    .macd(macdValues.get(i))
                    .signal(signalValues.get(i))
                    .oscillator(oscillatorValues.get(i))
                    .build();
            macds.add(macd);
        }

        return macds;
    }

}
