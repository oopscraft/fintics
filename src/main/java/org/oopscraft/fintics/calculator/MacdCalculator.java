package org.oopscraft.fintics.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MacdCalculator {

    private final List<BigDecimal> series;

    private final int shortPeriod;

    private final int longPeriod;

    private final int signalPeriod;

    public static MacdCalculator of(List<BigDecimal> series, int shortPeriod, int longPeriod, int signalPeriod) {
        return new MacdCalculator(series, shortPeriod, longPeriod, signalPeriod);
    }

    public MacdCalculator(List<BigDecimal> series, int shortPeriod, int longPeriod, int signalPeriod) {
        this.series = series;
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
        this.signalPeriod = signalPeriod;

    }

    public List<Macd> calculate() {
        List<BigDecimal> values = new ArrayList<>();
        List<BigDecimal> oscillators = new ArrayList<>();

        // Calculate MACD line
        List<BigDecimal> shortTermEma = EmaCalculator.of(series, shortPeriod)
                .calculate();
        List<BigDecimal> longTermEma = EmaCalculator.of(series, longPeriod)
                .calculate();
        for (int i = 0; i < longTermEma.size(); i++) {
            BigDecimal macd = shortTermEma.get(i).subtract(longTermEma.get(i));
            values.add(macd);
        }

        // ine using MACD line
        List<BigDecimal> signals = new ArrayList<>(EmaCalculator.of(values, signalPeriod)
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
                    .value(values.get(i).setScale(2,RoundingMode.HALF_UP))
                    .signal(signals.get(i).setScale(2, RoundingMode.HALF_UP))
                    .oscillator(oscillators.get(i).setScale(2, RoundingMode.HALF_UP))
                    .build();
            macds.add(macd);
        }
        return macds;
    }

}
