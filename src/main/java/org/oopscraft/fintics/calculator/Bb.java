package org.oopscraft.fintics.calculator;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@Getter
@ToString
public class Bb implements CalculateResult {

    private BigDecimal mbb;

    private BigDecimal ubb;

    private BigDecimal lbb;

    private BigDecimal bandWidth;

    private BigDecimal percentB;

}
