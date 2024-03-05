package org.oopscraft.fintics.calculator;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ChaikinOscillatorCalculator extends Calculator<ChaikinOscillatorContext, ChaikinOscillator> {

    public ChaikinOscillatorCalculator(ChaikinOscillatorContext context) {
        super(context);
    }

    @Override
    public List<ChaikinOscillator> calculate(List<Ohlcv> series) {
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
        List<BigDecimal> shortEmas = emas(adValues, getContext().getShortPeriod(), getContext().getMathContext());
        List<BigDecimal> longEmas = emas(adValues, getContext().getLongPeriod(), getContext().getMathContext());
        List<BigDecimal> values = new ArrayList<>();
        for(int i = 0; i < shortEmas.size(); i ++) {
            BigDecimal value = shortEmas.get(i).subtract(longEmas.get(i));
            values.add(value);
        }

        // signal
        List<BigDecimal> signals = emas(values, getContext().getSignalPeriod(), getContext().getMathContext());

        // chaikin's oscillator
        List<ChaikinOscillator> cos = new ArrayList<>();
        for(int i = 0; i < values.size(); i ++) {
            ChaikinOscillator co = ChaikinOscillator.builder()
                    .value(values.get(i).setScale(2, RoundingMode.HALF_UP))
                    .signal(signals.get(i).setScale(2, RoundingMode.HALF_UP))
                    .build();
            cos.add(co);
        }
        return cos;
    }

}
