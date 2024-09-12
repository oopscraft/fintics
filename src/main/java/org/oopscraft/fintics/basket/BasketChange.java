package org.oopscraft.fintics.basket;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class BasketChange {

    private String symbol;

    private BigDecimal holdingWeight;

    public static BasketChange of(String symbol, BigDecimal holdingWeight) {
        return BasketChange.builder()
                .symbol(symbol)
                .holdingWeight(holdingWeight)
                .build();
    }

}
