package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
public class MarketIndex {

    private final BigDecimal symbol;

    private final BigDecimal name;

    private final List<Ohlcv> minuteOhlcvs;

    private final List<Ohlcv> dailyOhlcvs;

}
