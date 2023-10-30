package org.oopscraft.fintics.calculator;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class Macd {

    @Builder.Default
    private BigDecimal macd = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal signal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal oscillator = BigDecimal.ZERO;

}
