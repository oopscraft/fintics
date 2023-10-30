package org.oopscraft.fintics.calculator;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    public Dmi setScale(int scale, RoundingMode roundingMode) {
        pdi = pdi.setScale(scale, roundingMode);
        mdi = mdi.setScale(scale, roundingMode);
        adx = adx.setScale(scale, roundingMode);
        return this;
    }

}
