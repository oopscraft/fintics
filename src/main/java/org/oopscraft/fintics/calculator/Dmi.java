package org.oopscraft.fintics.calculator;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@Getter
@ToString
public class Dmi {

    @Builder.Default
    private BigDecimal pdi = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal mdi = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal adx = BigDecimal.ZERO;

}
