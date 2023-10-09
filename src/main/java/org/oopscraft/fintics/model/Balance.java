package org.oopscraft.fintics.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Balance {

    private String accountNo;

    private BigDecimal total;

    private BigDecimal cash;

    @Builder.Default
    private List<BalanceAsset> balanceAssets = new ArrayList<>();

    public boolean hasBalanceAsset(String symbol) {
        return balanceAssets.stream()
                .anyMatch(e -> Objects.equals(e.getSymbol(), symbol));
    }

    public BalanceAsset getBalanceAsset(String symbol) {
        return balanceAssets.stream()
                .filter(balanceAsset ->
                    Objects.equals(balanceAsset.getSymbol(), symbol))
                .findFirst()
                .orElse(null);
    }

}
