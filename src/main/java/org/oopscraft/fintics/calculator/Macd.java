package org.oopscraft.fintics.calculator;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Builder
@Getter
public class Macd {

    @Builder.Default
    private BigDecimal macd = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal signal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal oscillator = BigDecimal.ZERO;

    public Macd setScale(int scale, RoundingMode roundingMode) {
        macd = macd.setScale(scale, roundingMode);
        signal = signal.setScale(scale, roundingMode);
        oscillator = oscillator.setScale(scale, roundingMode);
        return this;
    }

}
