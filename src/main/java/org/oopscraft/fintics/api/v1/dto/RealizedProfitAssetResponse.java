package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.RealizedProfit;
import org.oopscraft.fintics.model.RealizedProfitAsset;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RealizedProfitAssetResponse extends AssetResponse {

    private LocalDate date;

    private BigDecimal quantity;

    private BigDecimal purchasePrice;

    private BigDecimal purchaseAmount;

    private BigDecimal disposePrice;

    private BigDecimal disposeAmount;

    private BigDecimal feeAmount;

    private BigDecimal profitAmount;

    private BigDecimal profitPercentage;

    public static RealizedProfitAssetResponse from(RealizedProfitAsset realizedProfitAsset) {
        return RealizedProfitAssetResponse.builder()
                .assetId(realizedProfitAsset.getAssetId())
                .name(realizedProfitAsset.getName())
                .date(realizedProfitAsset.getDate())
                .quantity(realizedProfitAsset.getQuantity())
                .purchasePrice(realizedProfitAsset.getPurchasePrice())
                .purchaseAmount(realizedProfitAsset.getPurchaseAmount())
                .disposePrice(realizedProfitAsset.getDisposePrice())
                .disposeAmount(realizedProfitAsset.getDisposeAmount())
                .feeAmount(realizedProfitAsset.getFeeAmount())
                .profitAmount(realizedProfitAsset.getProfitAmount())
                .profitPercentage(realizedProfitAsset.getProfitPercentage())
                .build();
    }

}
