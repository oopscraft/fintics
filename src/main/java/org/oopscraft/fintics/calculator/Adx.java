package org.oopscraft.fintics.calculator;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Adx {

    @Builder.Default
    private Double value = 0.0;

    @Builder.Default
    private Double pdi = 0.0;

    @Builder.Default
    private Double mdi = 0.0;

}
