package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.RealizedProfit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class RealizedProfitResponse {

    private BigDecimal totalProfitAmount;

    private BigDecimal totalFeeAmount;

    private List<RealizedProfitAssetResponse> realizedProfitAssets = new ArrayList<>();

    /**
     * factory method
     * @param realizedProfit realized profit
     * @return realized profit response
     */
    public static RealizedProfitResponse from(RealizedProfit realizedProfit) {
        return RealizedProfitResponse.builder()
                .totalProfitAmount(realizedProfit.getTotalProfitAmount())
                .totalFeeAmount(realizedProfit.getTotalFeeAmount())
                .realizedProfitAssets(realizedProfit.getRealizedProfitAssets().stream()
                        .map(RealizedProfitAssetResponse::from)
                        .toList())
                .build();
    }

}
