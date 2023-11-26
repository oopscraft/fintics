package org.oopscraft.fintics.calculator;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WvadCalculator extends Calculator<WvadContext, Wvad> {

    public WvadCalculator(WvadContext context) {
        super(context);
    }

    @Override
    public List<Wvad> calculate(List<Ohlcv> series) {
        List<BigDecimal> wvadValues = new ArrayList<>();
        BigDecimal wvadValue = BigDecimal.ZERO;
        for (int i = 0; i < series.size(); i++) {
            Ohlcv ohlcv = series.get(i);
            BigDecimal closeOpenDiff = ohlcv.getClosePrice().subtract(ohlcv.getOpenPrice());
            BigDecimal highLowDiff = ohlcv.getHighPrice().subtract(ohlcv.getLowPrice());
            BigDecimal mfm;
            if(highLowDiff.compareTo(BigDecimal.ZERO) == 0) {
                mfm = BigDecimal.ZERO;
            }else{
                mfm = closeOpenDiff.divide(highLowDiff, getContext().getMathContext());
            }
            BigDecimal mfVolume = mfm.multiply(ohlcv.getVolume());
            wvadValue = wvadValue.add(mfVolume);
            wvadValues.add(new BigDecimal(wvadValue.unscaledValue(), wvadValue.scale()));
        }
        return wvadValues.stream()
                .map(value -> Wvad.builder()
                        .value(value)
                        .build())
                .collect(Collectors.toList());
    }

}
