package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@SuperBuilder
@Getter
@ToString
public class Dmi extends CalculateResult {

    private BigDecimal pdi;

    private BigDecimal mdi;

    private BigDecimal adx;

}
