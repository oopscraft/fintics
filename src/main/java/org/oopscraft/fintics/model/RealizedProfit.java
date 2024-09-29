package org.oopscraft.fintics.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RealizedProfit {

    private BigDecimal totalProfitAmount;

    private BigDecimal totalFeeAmount;

    private List<RealizedProfitAsset> realizedProfitAssets = new ArrayList<>();

}
