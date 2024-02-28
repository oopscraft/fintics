package org.oopscraft.fintics.calculator;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ObvCalculator extends Calculator<ObvContext, Obv> {

    public ObvCalculator(ObvContext context) {
        super(context);
    }

    @Override
    public List<Obv> calculate(List<Ohlcv> series) {
        // values
        List<BigDecimal> obvValues = new ArrayList<>();
        obvValues.add(BigDecimal.ZERO);
        BigDecimal obvValue = BigDecimal.ZERO;
        for(int i = 1, size = series.size(); i < size; i ++) {
            Ohlcv prevOhlcv = series.get(i-1);
            Ohlcv currentOhlcv = series.get(i);
            BigDecimal volume = currentOhlcv.getVolume();
            BigDecimal priceDiff = currentOhlcv.getClosePrice().subtract(prevOhlcv.getClosePrice());
            if(priceDiff.compareTo(BigDecimal.ZERO) > 0) {
                obvValue = obvValue.add(volume);
            }else if(priceDiff.compareTo(BigDecimal.ZERO) < 0) {
                obvValue = obvValue.subtract(volume);
            }
            obvValues.add(new BigDecimal(obvValue.unscaledValue(), obvValue.scale()));
        }

        // signal
        List<BigDecimal> signals = emas(obvValues, getContext().getSignalPeriod(), getContext().getMathContext()).stream()
                .map(value -> value.setScale(0, RoundingMode.HALF_UP))
                .collect(Collectors.toList());

        // obv
        List<Obv> obvs = new ArrayList<>();
        for(int i = 0; i < obvValues.size(); i ++) {
            Obv obv = Obv.builder()
                    .value(obvValues.get(i))
                    .signal(signals.get(i))
                    .build();
            obvs.add(obv);
        }
        return obvs;
    }


}
