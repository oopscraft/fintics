package org.oopscraft.fintics.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Profit {

    private BigDecimal totalProfitAmount;

    private BigDecimal realizedProfitAmount;

    private BigDecimal dividendAmount;

    private List<RealizedProfit> realizedProfits = new ArrayList<>();

    private List<DividendHistory> dividendHistories = new ArrayList<>();

}
