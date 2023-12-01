package org.oopscraft.fintics.calculator;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class SmaCalculator extends Calculator<SmaContext, Sma> {

    public SmaCalculator(SmaContext context) {
        super(context);
    }

    @Override
    public List<Sma> calculate(List<Ohlcv> series) {
        List<BigDecimal> closePrices = series.stream()
                .map(Ohlcv::getClosePrice)
                .toList();

        return smas(closePrices, getContext().getPeriod()).stream()
                .map(value -> Sma.builder()
                        .value(value)
                        .build())
                .collect(Collectors.toList());
    }

}
