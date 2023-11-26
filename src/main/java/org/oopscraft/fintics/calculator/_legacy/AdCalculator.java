package org.oopscraft.fintics.calculator._legacy;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class AdCalculator {


    private final List<Ohlcv> series;

    private final MathContext mathContext;

    public static AdCalculator of(List<Ohlcv> series) {
        return of(series, new MathContext(4, RoundingMode.HALF_UP));
    }

    public static AdCalculator of(List<Ohlcv> series, MathContext mathContext) {
        return new AdCalculator(series, mathContext);
    }

    public AdCalculator(List<Ohlcv> series, MathContext mathContext) {
        this.series = series;
        this.mathContext = mathContext;
    }

    public List<BigDecimal> calculate() {
        List<BigDecimal> ads = new ArrayList<BigDecimal>();
        BigDecimal ad = BigDecimal.ZERO;
        for (int i = 0; i < series.size(); i++) {
            Ohlcv ohlcv = series.get(i);
            BigDecimal closeLowDiff = ohlcv.getClosePrice().subtract(ohlcv.getLowPrice());
            BigDecimal highCloseDiff = ohlcv.getHighPrice().subtract(ohlcv.getClosePrice());
            BigDecimal highLowDiff = ohlcv.getHighPrice().subtract(ohlcv.getLowPrice());
            BigDecimal mfm;
            if(highLowDiff.compareTo(BigDecimal.ZERO) == 0) {
                mfm = BigDecimal.ZERO;
            }else{
                mfm = closeLowDiff.subtract(highCloseDiff)
                        .divide(highLowDiff, mathContext);
            }
            BigDecimal mfVolume = mfm.multiply(ohlcv.getVolume());
            ad = ad.add(mfVolume);
            ads.add(new BigDecimal(ad.unscaledValue(), ad.scale()));
        }
        return ads;
    }

}
