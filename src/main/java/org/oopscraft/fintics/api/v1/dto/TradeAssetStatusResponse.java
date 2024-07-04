package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
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

    private BigDecimal previousClose;

    private BigDecimal open;

    private BigDecimal close;

    private BigDecimal netChange;

    private BigDecimal netChangePercentage;

    private BigDecimal intraDayNetChange;

    private BigDecimal intraDayNetChangePercentage;

    private String message;

    public static TradeAssetStatusResponse from(TradeAssetStatus tradeAssetStatus) {
        return TradeAssetStatusResponse.builder()
                .tradeId(tradeAssetStatus.getTradeId())
                .assetId(tradeAssetStatus.getAssetId())
                .previousClose(tradeAssetStatus.getPreviousClose())
                .open(tradeAssetStatus.getOpen())
                .close(tradeAssetStatus.getClose())
                .netChange(tradeAssetStatus.getNetChange())
                .netChangePercentage(tradeAssetStatus.getNetChangePercentage())
                .intraDayNetChange(tradeAssetStatus.getIntraDayNetChange())
                .intraDayNetChangePercentage(tradeAssetStatus.getIntraDayNetChangePercentage())
                .message(tradeAssetStatus.getMessage())
                .build();
    }

}
