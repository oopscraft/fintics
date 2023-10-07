package org.oopscraft.fintics.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Balance {

    private BigDecimal total;

    private BigDecimal cash;

    @Builder.Default
    private List<BalanceAsset> balanceAssets = new ArrayList<>();

}
