package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BalanceAsset extends Asset {

    private String accountNo;

    private String symbol;

    private String name;

    private Integer quantity;

    private Double purchaseAmount;

    private Double valuationAmount;

    private Double gainLossAmount;

}
