package org.oopscraft.fintics.calculator;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdCalculator extends Calculator<AdContext, Ad> {

    public AdCalculator(AdContext context) {
        super(context);
    }

    @Override
    public List<Ad> calculate(List<Ohlcv> series) {
        List<BigDecimal> adValues = new ArrayList<BigDecimal>();
        BigDecimal adValue = BigDecimal.ZERO;
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
                        .divide(highLowDiff, getContext().getMathContext());
            }
            BigDecimal mfVolume = mfm.multiply(ohlcv.getVolume());
            adValue = adValue.add(mfVolume);
            adValues.add(new BigDecimal(adValue.unscaledValue(), adValue.scale()));
        }
        return adValues.stream()
                .map(value -> Ad.builder()
                        .value(value)
                        .build())
                .collect(Collectors.toList());
    }

}
