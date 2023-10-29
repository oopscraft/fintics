package org.oopscraft.fintics.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class AdxCalculator {

    private final List<Double> highSeries;

    private final List<Double> lowSeries;

    private final List<Double> closeSeries;

    private final int period;

    public static AdxCalculator of(List<Double> highSeries, List<Double> lowSeries, List<Double> closeSeries, int period) {
        return new AdxCalculator(highSeries, lowSeries, closeSeries, period);
    }

    public AdxCalculator(List<Double> highSeries, List<Double> lowSeries, List<Double> closeSeries, int period) {
        this.highSeries = highSeries;
        this.lowSeries = lowSeries;
        this.closeSeries = closeSeries;
        this.period = period;
    }

    public List<Adx> calculate() {

        List<Double> pdms = new ArrayList<>();
        List<Double> mdms = new ArrayList<>();
        List<Double> trs = new ArrayList<>();
        for(int i = 0; i < highSeries.size(); i ++ ) {
            double high = highSeries.get(i);
            double low = lowSeries.get(i);
            double previousHigh = highSeries.get(Math.max(i-1, 0));
            double previousLow = lowSeries.get(Math.max(i-1, 0));
            double previousClose = closeSeries.get(Math.max(i-1, 0));

            // calculates
            double pdm = high > previousHigh && (high - previousHigh) > (previousLow - low)
                    ? high - previousHigh
                    : 0.0;
            double mdm = previousLow > low && (high - previousHigh) < (previousLow - low)
                    ? previousLow - low
                    : 0.0;

            double s1 = high - low;
            double s2 = Math.abs(previousClose - high);
            double s3 = Math.abs(previousClose - low);
            Double tr = Math.max(Math.max(s1, s2), s3);

            pdms.add(pdm);
            mdms.add(mdm);
            trs.add(tr);
        }

        List<Double> pdis = new ArrayList<>();
        List<Double> mdis = new ArrayList<>();
        List<Double> dxs = new ArrayList<>();
        for(int i = 0; i < highSeries.size(); i ++) {
            int fromIndex = Math.max(i - period + 1, 0);
            int toIndex = i + 1;
            List<Double> periodPdms = pdms.subList(fromIndex, toIndex);
            List<Double> periodMdms = mdms.subList(fromIndex, toIndex);
            List<Double> periodTrs = trs.subList(fromIndex, toIndex);

            // di
            double pdi = calculateDi(periodPdms, periodTrs);
            double mdi = calculateDi(periodMdms, periodTrs);
            pdis.add(pdi);
            mdis.add(mdi);

            // dx
            double dx = calculateDx(pdi, mdi);
            dxs.add(dx);
        }

        // adx
        List<Double> adxs = new ArrayList<>();
        for(int i = 0; i < dxs.size(); i ++) {
            int fromIndex = Math.max(i - period + 1, 0);
            int toIndex = i + 1;
            List<Double> periodDxs = dxs.subList(fromIndex, toIndex);
            double adx = calculateAdx(periodDxs);
            adxs.add(adx);
        }


        // return Dmi
        List<Adx> dmis = new ArrayList<>();
        for(int i = 0; i < pdis.size(); i ++ ) {
            Adx dmi = Adx.builder()
                    .value(adxs.get(i))
                    .pdi(pdis.get(i))
                    .mdi(mdis.get(i))
                    .build();
            dmis.add(dmi);
        }
        return dmis;
    }

    private static Double calculateDi(List<Double> dms, List<Double> trs) {
        if(dms.isEmpty() || trs.isEmpty()) {
            return 0.0;
        }

        BigDecimal sumOfDms = BigDecimal.ZERO;
        for(Double dm : dms) {
            sumOfDms = sumOfDms.add(BigDecimal.valueOf(dm));
        }
        BigDecimal averageOfDms = sumOfDms.divide(BigDecimal.valueOf(dms.size()), MathContext.DECIMAL128);

        BigDecimal sumOfTrs = BigDecimal.ZERO;
        for(Double trueRange : trs) {
            sumOfTrs = sumOfTrs.add(BigDecimal.valueOf(trueRange));
        }
        BigDecimal averageOfTrs = sumOfTrs.divide(BigDecimal.valueOf(trs.size()), MathContext.DECIMAL128);

        double di;
        if(averageOfTrs.doubleValue() > 0) {
            di = averageOfDms.divide(averageOfTrs, MathContext.DECIMAL128)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }else{
            di = 0.0;
        }
        return di;
    }

    private static Double calculateDx(Double pdi, Double mdi) {
        if(pdi + mdi == 0) {
           return 0.0;
        }
        return BigDecimal.valueOf(Math.abs(pdi - mdi))
                .divide(BigDecimal.valueOf(pdi + mdi), MathContext.DECIMAL128)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static Double calculateAdx(List<Double> dxs) {
        if(dxs.isEmpty()) {
            return 0.0;
        }
        BigDecimal adx = BigDecimal.ZERO;
        for(Double dx : dxs) {
            adx = adx.add(BigDecimal.valueOf(dx));
        }
        return adx.divide(BigDecimal.valueOf(dxs.size()), MathContext.DECIMAL128)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }


}
