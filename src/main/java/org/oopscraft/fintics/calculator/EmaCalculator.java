package org.oopscraft.fintics.calculator;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class EmaCalculator extends Calculator<EmaContext, Ema> {

    public EmaCalculator(EmaContext context) {
        super(context);
    }

    @Override
    public List<Ema> calculate(List<Ohlcv> series) {
        List<BigDecimal> closePrices = series.stream()
                .map(Ohlcv::getClosePrice)
                .toList();

        List<BigDecimal> emaValues = this.emas(closePrices, getContext().getPeriod());

        return emaValues.stream()
                .map(emaValue -> Ema.builder()
                        .value(emaValue)
                        .build())
                .collect(Collectors.toList());
    }

}
