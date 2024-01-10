package org.oopscraft.fintics.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Balance {

    private String accountNo;

    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal cashAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal purchaseAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal valuationAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal profitAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal realizedProfitAmount = BigDecimal.ZERO;

    @Builder.Default
    private List<BalanceAsset> balanceAssets = new ArrayList<>();

    public boolean hasBalanceAsset(String symbol) {
        return balanceAssets.stream()
                .anyMatch(e -> Objects.equals(e.getAssetId(), symbol));
    }

    public Optional<BalanceAsset> getBalanceAsset(String symbol) {
        return balanceAssets.stream()
                .filter(balanceAsset ->
                    Objects.equals(balanceAsset.getAssetId(), symbol))
                .findFirst();
    }

    public void addBalanceAsset(BalanceAsset balanceAsset) {
        this.balanceAssets.add(balanceAsset);
    }

    public void removeBalanceAsset(BalanceAsset balanceAsset) {
        this.balanceAssets.remove(balanceAsset);
    }

}
