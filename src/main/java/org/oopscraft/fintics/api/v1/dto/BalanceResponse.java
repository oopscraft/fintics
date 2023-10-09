package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Balance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BalanceResponse {

    private String accountNo;

    private BigDecimal total;

    private BigDecimal cash;

    @Builder.Default
    private List<BalanceAssetResponse> balanceAssets = new ArrayList<>();

    public static BalanceResponse from(Balance balance) {
        BalanceResponse balanceResponse = BalanceResponse.builder()
                .accountNo(balance.getAccountNo())
                .total(balance.getTotal())
                .cash(balance.getCash())
                .build();
        List<BalanceAssetResponse> balanceAssetResponses = balance.getBalanceAssets().stream()
                .map(BalanceAssetResponse::from)
                .collect(Collectors.toList());
        balanceResponse.setBalanceAssets(balanceAssetResponses);
        return balanceResponse;
    }


}
