package org.oopscraft.fintics.calculator._legacy;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ObvCalculator {

    private final List<Ohlcv> series;

    private final MathContext mathContext;

    public static ObvCalculator of(List<Ohlcv> series) {
        return of(series, new MathContext(4, RoundingMode.HALF_UP));
    }

    public static ObvCalculator of(List<Ohlcv> series, MathContext mathContext) {
        return new ObvCalculator(series, mathContext);
    }

    public ObvCalculator(List<Ohlcv> series, MathContext mathContext) {
        this.series = series;
        this.mathContext = mathContext;
    }

    public List<BigDecimal> calculate() {
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
        return obvValues;
    }

}
