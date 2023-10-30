package org.oopscraft.fintics.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class DmiCalculator {

    private final List<BigDecimal> highSeries;

    private final List<BigDecimal> lowSeries;

    private final List<BigDecimal> closeSeries;

    private final int period;

    public static DmiCalculator of(List<BigDecimal> highSeries, List<BigDecimal> lowSeries, List<BigDecimal> closeSeries, int period) {
        return new DmiCalculator(highSeries, lowSeries, closeSeries, period);
    }

    public DmiCalculator(List<BigDecimal> highSeries, List<BigDecimal> lowSeries, List<BigDecimal> closeSeries, int period) {
        this.highSeries = highSeries;
        this.lowSeries = lowSeries;
        this.closeSeries = closeSeries;
        this.period = period;
    }

    public List<Dmi> calculate() {
        List<BigDecimal> pdms = new ArrayList<>();
        List<BigDecimal> mdms = new ArrayList<>();
        List<BigDecimal> trs = new ArrayList<>();
        for(int i = 0; i < highSeries.size(); i ++ ) {
            BigDecimal high = highSeries.get(i);
            BigDecimal low = lowSeries.get(i);
            BigDecimal previousHigh = highSeries.get(Math.max(i-1, 0));
            BigDecimal previousLow = lowSeries.get(Math.max(i-1, 0));
            BigDecimal previousClose = closeSeries.get(Math.max(i-1, 0));

            BigDecimal pdm = calculatePdm(high, low, previousHigh, previousLow);
            BigDecimal mdm = calculateMdm(high, low, previousHigh, previousLow);
            BigDecimal tr = calculateTr(high, low, previousClose);

            pdms.add(pdm);
            mdms.add(mdm);
            trs.add(tr);
        }

        // average
        pdms = EmaCalculator.of(pdms, period).calculate();
        mdms = EmaCalculator.of(mdms, period).calculate();
        trs = EmaCalculator.of(trs, period).calculate();

        List<BigDecimal> pdis = new ArrayList<>();
        List<BigDecimal> mdis = new ArrayList<>();
        List<BigDecimal> dxs = new ArrayList<>();
        for(int i = 0; i < highSeries.size(); i ++) {
            BigDecimal pdi = calculatePdi(pdms.get(i), trs.get(i));
            BigDecimal mdi = calculateMdi(mdms.get(i), trs.get(i));
            pdis.add(pdi);
            mdis.add(mdi);

            // dx
            BigDecimal dx = calculateDx(pdi, mdi);
            dxs.add(dx);
        }

        // average
        dxs = EmaCalculator.of(dxs, period).calculate();

        // dmi
        List<Dmi> dmis = new ArrayList<>();
        for(int i = 0; i < highSeries.size(); i ++) {
            Dmi dmi = Dmi.builder()
                    .adx(dxs.get(i))
                    .pdi(pdis.get(i))
                    .mdi(mdis.get(i))
                    .build();
            dmis.add(dmi);
        }
        return dmis;
    }

    private BigDecimal calculatePdm(BigDecimal high, BigDecimal low, BigDecimal previousHigh, BigDecimal previousLow) {
        BigDecimal upMove = high.subtract(previousHigh);
        BigDecimal downMove = previousLow.subtract(low);
        if(upMove.compareTo(downMove) > 0 && upMove.compareTo(BigDecimal.ZERO) > 0) {
            return upMove;
        }else{
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateMdm(BigDecimal high, BigDecimal low, BigDecimal previousHigh, BigDecimal previousLow) {
        BigDecimal upMove = high.subtract(previousHigh);
        BigDecimal downMove = previousLow.subtract(low);
        if(downMove.compareTo(upMove) > 0 && downMove.compareTo(BigDecimal.ZERO) > 0) {
            return downMove;
        }else{
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateTr(BigDecimal high, BigDecimal low, BigDecimal previousClose) {
        BigDecimal hl = high.subtract(low);
        BigDecimal hc = high.subtract(previousClose);
        BigDecimal cl = previousClose.subtract(low);
        return hl.abs().max(hc.abs()).max(cl.abs());
    }

    private BigDecimal calculatePdi(BigDecimal pdm, BigDecimal tr) {
        return pdm.divide(tr, MathContext.DECIMAL128)
                .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal calculateMdi(BigDecimal mdm, BigDecimal tr) {
        return mdm.divide(tr, MathContext.DECIMAL128)
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateDx(BigDecimal pdi, BigDecimal mdi) {
        if(pdi.add(mdi).compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return pdi.subtract(mdi).abs()
                .divide(pdi.add(mdi), MathContext.DECIMAL128)
                .multiply(BigDecimal.valueOf(100));
    }

}
