package org.oopscraft.fintics.calculator;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CoCalculator extends Calculator<CoContext, Co> {

    public CoCalculator(CoContext context) {
        super(context);
    }

    @Override
    public List<Co> calculate(List<Ohlcv> series) {
        // ad values
        List<BigDecimal> adValues = new ArrayList<>();
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

        // values
        List<BigDecimal> shortEmas = emas(adValues, getContext().getShortPeriod());
        List<BigDecimal> longEmas = emas(adValues, getContext().getLongPeriod());
        List<BigDecimal> values = new ArrayList<>();
        for(int i = 0; i < shortEmas.size(); i ++) {
            BigDecimal value = shortEmas.get(i).subtract(longEmas.get(i));
            values.add(value);
        }

        // signal
        List<BigDecimal> signals = emas(values, getContext().getSignalPeriod());

        // chaikin's oscillator
        List<Co> cos = new ArrayList<>();
        for(int i = 0; i < values.size(); i ++) {
            Co co = Co.builder()
                    .value(values.get(i).setScale(2, RoundingMode.HALF_UP))
                    .signal(signals.get(i).setScale(2, RoundingMode.HALF_UP))
                    .build();
            cos.add(co);
        }
        return cos;
    }

}
