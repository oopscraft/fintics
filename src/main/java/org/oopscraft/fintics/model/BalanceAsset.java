package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

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

    private Double purchaseAmount;

    private Double valuationAmount;

    private Double gainLossAmount;

}
