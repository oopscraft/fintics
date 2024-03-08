package org.oopscraft.fintics.calculator;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DisparityIndexCalculator extends Calculator<DisparityIndexContext, DisparityIndex> {

    public DisparityIndexCalculator(DisparityIndexContext context) {
        super(context);
    }

    @Override
    public List<DisparityIndex> calculate(List<Ohlcv> series) {
        List<DisparityIndex> disparityIndexes = new ArrayList<>();
        // close prices
        List<BigDecimal> closePrices = series.stream()
                .map(Ohlcv::getClosePrice)
                .toList();
        // ema
        List<BigDecimal> emas = emas(closePrices, getContext().getPeriod(), getContext().getMathContext());

        for (int i = 0; i < closePrices.size(); i ++) {
            BigDecimal currentPrice = closePrices.get(i);
            BigDecimal currentEma = emas.get(i);
            BigDecimal disparityIndexValue = currentPrice.subtract(currentEma)
                    .divide(currentEma, getContext().getMathContext())
                    .multiply(BigDecimal.valueOf(100));
            disparityIndexes.add(DisparityIndex.builder()
                    .dateTime(series.get(i).getDateTime())
                    .value(disparityIndexValue)
                    .build());
        }
        return disparityIndexes;
    }

}
