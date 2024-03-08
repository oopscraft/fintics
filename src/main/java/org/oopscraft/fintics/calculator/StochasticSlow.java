package org.oopscraft.fintics.calculator;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@Getter
@ToString
public class StochasticSlow implements CalculateResult {

    private BigDecimal slowK;

    private BigDecimal slowD;

}
