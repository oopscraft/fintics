package org.oopscraft.fintics.calculator;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@Getter
@ToString
public class BollingerBand implements CalculateResult {

    private BigDecimal middle;

    private BigDecimal upper;

    private BigDecimal lower;

    private BigDecimal bandWidth;

    private BigDecimal percentB;

}
