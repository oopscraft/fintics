package org.oopscraft.fintics.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class AdxCalculator {

    private final List<BigDecimal> highSeries;

    private final List<BigDecimal> lowSeries;

    private final List<BigDecimal> closeSeries;

    private final int period;

    public static AdxCalculator of(List<BigDecimal> highSeries, List<BigDecimal> lowSeries, List<BigDecimal> closeSeries, int period) {
        return new AdxCalculator(highSeries, lowSeries, closeSeries, period);
    }

    public AdxCalculator(List<BigDecimal> highSeries, List<BigDecimal> lowSeries, List<BigDecimal> closeSeries, int period) {
        this.highSeries = highSeries;
        this.lowSeries = lowSeries;
        this.closeSeries = closeSeries;
        this.period = period;
    }

    public List<Adx> calculate() {
        List<BigDecimal> pdms = new ArrayList<>();
        List<BigDecimal> mdms = new ArrayList<>();
        List<BigDecimal> trs = new ArrayList<>();
        for(int i = 0; i < highSeries.size(); i ++ ) {
            BigDecimal high = highSeries.get(i);
            BigDecimal low = lowSeries.get(i);
            BigDecimal previousHigh = highSeries.get(Math.max(i-1, 0));
            BigDecimal previousLow = lowSeries.get(Math.max(i-1, 0));
            BigDecimal previousClose = closeSeries.get(Math.max(i-1, 0));

            // calculates
            BigDecimal pdm = high.compareTo(previousHigh) > 0 && high.subtract(previousHigh).compareTo(previousLow.subtract(low)) > 0
                    ? high.subtract(previousHigh)
                    : BigDecimal.ZERO;
            BigDecimal mdm = previousLow.compareTo(low) > 0 && high.subtract(previousHigh).compareTo(previousLow.subtract(low)) < 0
                    ? previousLow.subtract(low)
                    : BigDecimal.ZERO;

            BigDecimal tr1 = high.subtract(low);
            BigDecimal tr2 = high.subtract(previousClose).abs();
            BigDecimal tr3 = previousClose.subtract(low).abs();
            BigDecimal tr = tr1.max(tr2).max(tr3);

            pdms.add(pdm);
            mdms.add(mdm);
            trs.add(tr);
        }

//        // test
//        pdms = MmaCalculator.of(pdms, period).calculate();
//        mdms = MmaCalculator.of(mdms, period).calculate();
//        trs = MmaCalculator.of(trs, period).calculate();


        List<BigDecimal> pdis = new ArrayList<>();
        List<BigDecimal> mdis = new ArrayList<>();
        List<BigDecimal> dxs = new ArrayList<>();
        for(int i = 0; i < highSeries.size(); i ++) {
            int fromIndex = Math.max(i - period + 1, 0);
            int toIndex = i + 1;
            List<BigDecimal> periodPdms = pdms.subList(fromIndex, toIndex);
            List<BigDecimal> periodMdms = mdms.subList(fromIndex, toIndex);
            List<BigDecimal> periodTrs = trs.subList(fromIndex, toIndex);

            // di
            BigDecimal pdi = calculateDi(periodPdms, periodTrs);
            BigDecimal mdi = calculateDi(periodMdms, periodTrs);

//            pdi = pdms.get(i)
//                    .divide(trs.get(i), MathContext.DECIMAL128)
//                    .multiply(BigDecimal.valueOf(100));
//            mdi =  mdms.get(i)
//                    .divide(trs.get(i), MathContext.DECIMAL128)
//                    .multiply(BigDecimal.valueOf(100));

            pdis.add(pdi);
            mdis.add(mdi);

            // dx
            BigDecimal dx = calculateDx(pdi, mdi);
            dxs.add(dx);
        }

        // test
//        List<Double> adxs = MmaCalculator.of(dxs, period).calculate();

        // adx
        List<BigDecimal> adxValues = new ArrayList<>();
        for(int i = 0; i < dxs.size(); i ++) {
            int fromIndex = Math.max(i - period + 1, 0);
            int toIndex = i + 1;
            List<BigDecimal> periodDxs = dxs.subList(fromIndex, toIndex);
            BigDecimal adxValue = calculateAdx(periodDxs);
            adxValues.add(adxValue);
        }


        // return
        List<Adx> adxs = new ArrayList<>();
        for(int i = 0; i < pdis.size(); i ++ ) {
            Adx dmi = Adx.builder()
                    .value(adxValues.get(i).setScale(2, RoundingMode.HALF_UP))
                    .pdi(pdis.get(i).setScale(2, RoundingMode.HALF_UP))
                    .mdi(mdis.get(i).setScale(2, RoundingMode.HALF_UP))
                    .build();
            adxs.add(dmi);
        }
        return adxs;
    }

    private static BigDecimal calculateDi(List<BigDecimal> dms, List<BigDecimal> trs) {
        if(dms.isEmpty() || trs.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sumOfDms = BigDecimal.ZERO;
        for(BigDecimal dm : dms) {
            sumOfDms = sumOfDms.add(dm);
        }
        BigDecimal averageOfDms = sumOfDms.divide(BigDecimal.valueOf(dms.size()), MathContext.DECIMAL128);

        BigDecimal sumOfTrs = BigDecimal.ZERO;
        for(BigDecimal trueRange : trs) {
            sumOfTrs = sumOfTrs.add(trueRange);
        }
        BigDecimal averageOfTrs = sumOfTrs.divide(BigDecimal.valueOf(trs.size()), MathContext.DECIMAL128);

        BigDecimal di;
        if(averageOfTrs.doubleValue() > 0) {
            di = averageOfDms.divide(averageOfTrs, MathContext.DECIMAL128)
                    .multiply(BigDecimal.valueOf(100));
        }else{
            di = BigDecimal.ZERO;
        }
        return di;
    }

    private static BigDecimal calculateDx(BigDecimal pdi, BigDecimal mdi) {
        if(pdi.add(mdi).compareTo(BigDecimal.ZERO) == 0) {
           return BigDecimal.ZERO;
        }
        return pdi.subtract(mdi).abs()
                .divide(pdi.add(mdi), MathContext.DECIMAL128)
                .multiply(BigDecimal.valueOf(100));
    }

    private static BigDecimal calculateAdx(List<BigDecimal> dxs) {
        if(dxs.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal adx = BigDecimal.ZERO;
        for(BigDecimal dx : dxs) {
            adx = adx.add(dx);
        }
        return adx.divide(BigDecimal.valueOf(dxs.size()), MathContext.DECIMAL128);
    }


}
