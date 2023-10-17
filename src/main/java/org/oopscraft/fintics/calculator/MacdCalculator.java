package org.oopscraft.fintics.calculator;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MacdCalculator {

    @Getter
    private final List<Double> macds = new ArrayList<>();

    @Getter
    private final List<Double> signals = new ArrayList<>();

    @Getter
    private final List<Double> oscillators = new ArrayList<>();

    public static MacdCalculator of(List<Double> series, int shortTermPeriod, int longTermPeriod, int signalPeriod) {
        return new MacdCalculator(series, shortTermPeriod, longTermPeriod, signalPeriod);
    }

    public MacdCalculator(List<Double> series, int shortTermPeriod, int longTermPeriod, int signalPeriod) {

        // Calculate MACD line
        List<Double> shortTermEMA = EmaCalculator.of(series, shortTermPeriod).getEmas();
        List<Double> longTermEMA = EmaCalculator.of(series, longTermPeriod).getEmas();
        for (int i = 0; i < longTermEMA.size(); i++) {
            double macd = shortTermEMA.get(i) - longTermEMA.get(i);
            macds.add(macd);
        }

        // ine using MACD line
        signals.addAll(EmaCalculator.of(macds, signalPeriod).getEmas());

        // oscillator
        for (int i = 0; i < macds.size(); i++) {
            double oscillator = macds.get(i) - signals.get(i);
            oscillators.add(BigDecimal.valueOf(oscillator)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue());
        }
    }

}
