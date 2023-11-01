package org.oopscraft.fintics.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BalanceAsset {

    private String accountNo;

    private String symbol;

    private String name;

    private AssetType type;

    private Integer quantity;

    private Integer orderableQuantity;

    private BigDecimal purchaseAmount;

    private BigDecimal valuationAmount;

    private BigDecimal profitAmount;

}
