package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.BalanceAsset;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BalanceAssetResponse {

    private String accountNo;

    private String symbol;

    private String name;

    private BigDecimal quantity;

    private BigDecimal purchaseAmount;

    private BigDecimal valuationAmount;

    private BigDecimal gainLossAmount;

    public static BalanceAssetResponse from(BalanceAsset balanceAsset) {
        return BalanceAssetResponse.builder()
                .accountNo(balanceAsset.getAccountNo())
                .symbol(balanceAsset.getSymbol())
                .name(balanceAsset.getName())
                .quantity(balanceAsset.getQuantity())
                .purchaseAmount(balanceAsset.getPurchaseAmount())
                .valuationAmount(balanceAsset.getValuationAmount())
                .gainLossAmount(balanceAsset.getGainLossAmount())
                .build();
    }
}
