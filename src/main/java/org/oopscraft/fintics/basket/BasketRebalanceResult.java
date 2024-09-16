package org.oopscraft.fintics.basket;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class BasketRebalanceResult {

    private String symbol;

    private String name;

    private BigDecimal holdingWeight;

    public static BasketRebalanceResult of(String symbol, String name, BigDecimal holdingWeight) {
        return BasketRebalanceResult.builder()
                .symbol(symbol)
                .name(name)
                .holdingWeight(holdingWeight)
                .build();
    }

}
