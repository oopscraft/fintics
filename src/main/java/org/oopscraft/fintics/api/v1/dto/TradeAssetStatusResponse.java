package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.TradeAsset;
import org.oopscraft.fintics.model.TradeAssetStatus;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAssetStatusResponse extends AssetResponse {

    private String tradeId;

    private String assetId;

    private BigDecimal previousClosePrice;

    private BigDecimal openPrice;

    private BigDecimal closePrice;

    private BigDecimal netChange;

    private BigDecimal netChangePercentage;

    private BigDecimal intraDayNetChange;

    private BigDecimal intraDayNetChangePercentage;

    private String message;

    public static TradeAssetStatusResponse from(TradeAssetStatus tradeAssetStatus) {
        return TradeAssetStatusResponse.builder()
                .tradeId(tradeAssetStatus.getTradeId())
                .assetId(tradeAssetStatus.getAssetId())
                .previousClosePrice(tradeAssetStatus.getPreviousClosePrice())
                .openPrice(tradeAssetStatus.getOpenPrice())
                .closePrice(tradeAssetStatus.getClosePrice())
                .netChange(tradeAssetStatus.getNetChange())
                .netChangePercentage(tradeAssetStatus.getNetChangePercentage())
                .intraDayNetChange(tradeAssetStatus.getIntraDayNetChange())
                .intraDayNetChangePercentage(tradeAssetStatus.getIntraDayNetChangePercentage())
                .message(tradeAssetStatus.getMessage())
                .build();
    }

}
