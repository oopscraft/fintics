package org.oopscraft.fintics.calculator;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MacdCalculator {

    public static List<BigDecimal> calculate(List<BigDecimal> prices, int shortTermPeriod, int longTermPeriod, int signalPeriod) {
        List<BigDecimal> shortTermEMA = EmaCalculator.calculate(prices, shortTermPeriod);
        List<BigDecimal> longTermEMA = EmaCalculator.calculate(prices, longTermPeriod);
        List<BigDecimal> macdValues = new ArrayList<>();

        // Calculate MACD line
        for (int i = 0; i < longTermEMA.size(); i++) {
            BigDecimal macd = shortTermEMA.get(i).subtract(longTermEMA.get(i));
            macdValues.add(macd);
        }

        // Calculate Signal line using MACD line
        List<BigDecimal> signalLine = EmaCalculator.calculate(macdValues, signalPeriod);

        // histogram
        List<BigDecimal> macdHistogram = new ArrayList<>();
        for (int i = 0; i < macdValues.size(); i++) {
            Mean mean = new Mean();
            double periodAverageMacd = mean.evaluate(macdValues.subList(Math.max(i-signalPeriod,0), i+1).stream()
                    .mapToDouble(BigDecimal::doubleValue)
                    .toArray());
            BigDecimal histogram = macdValues.get(i)
                    .subtract(BigDecimal.valueOf(periodAverageMacd))
                    .setScale(2, RoundingMode.HALF_UP);
            macdHistogram.add(histogram);
        }

        return macdHistogram;
    }

}
