package org.oopscraft.fintics.calculator;

import java.util.ArrayList;
import java.util.List;

public class MacdCalculator {

    private List<Double> closingPrices;

    public MacdCalculator(List<Double> closingPrices) {
        this.closingPrices = closingPrices;
    }

    public List<Double> calculateMacd(int shortTermPeriod, int longTermPeriod, int signalPeriod) {
        List<Double> emaShortTerm = calculateEma(shortTermPeriod);
        List<Double> emaLongTerm = calculateEma(longTermPeriod);
        List<Double> macdLine = calculateMacdLine(emaShortTerm, emaLongTerm);
        List<Double> signalLine = calculateSignalLine(macdLine, signalPeriod);

        List<Double> macdHistogram = new ArrayList<>();
        for (int i = 0; i < macdLine.size(); i++) {
            macdHistogram.add(macdLine.get(i) - signalLine.get(i));
        }

        return macdHistogram;
    }

    private List<Double> calculateEma(int period) {
        List<Double> emaValues = new ArrayList<>();
        double smoothingFactor = 2.0 / (period + 1);

        // Initialize with the first closing price
        emaValues.add(closingPrices.get(0));

        for (int i = 1; i < closingPrices.size(); i++) {
            double ema = (closingPrices.get(i) - emaValues.get(i - 1)) * smoothingFactor + emaValues.get(i - 1);
            emaValues.add(ema);
        }

        return emaValues;
    }

    private List<Double> calculateMacdLine(List<Double> shortTermEMA, List<Double> longTermEMA) {
        List<Double> macdLine = new ArrayList<>();
        int minLength = Math.min(shortTermEMA.size(), longTermEMA.size());

        for (int i = 0; i < minLength; i++) {
            macdLine.add(shortTermEMA.get(i) - longTermEMA.get(i));
        }

        return macdLine;
    }

    private List<Double> calculateSignalLine(List<Double> macdLine, int signalPeriod) {
        List<Double> signalLine = new ArrayList<>();

        // Initialize with zeros
        for (int i = 0; i < signalPeriod - 1; i++) {
            signalLine.add(0.0);
        }

        for (int i = signalPeriod - 1; i < macdLine.size(); i++) {
            double sum = 0;
            for (int j = 0; j < signalPeriod; j++) {
                sum += macdLine.get(i - j);
            }
            signalLine.add(sum / signalPeriod);
        }

        return signalLine;
    }


}
