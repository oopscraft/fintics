package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.dao.TradeAssetEntity;
import org.oopscraft.fintics.dao.TradeAssetStatusEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAssetStatus {

    private String tradeId;

    private String assetId;

    private BigDecimal previousClosePrice;

    private BigDecimal openPrice;

    private BigDecimal closePrice;

    private String message;

    public BigDecimal getNetChange() {
        return (closePrice != null ? closePrice : BigDecimal.ZERO)
                .subtract(previousClosePrice != null ? previousClosePrice : BigDecimal.ZERO);
    }

    public BigDecimal getNetChangePercentage() {
        if (previousClosePrice == null || previousClosePrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // 이전 종가가 없거나 0이면 비율을 계산할 수 없음
        }
        return getNetChange()
                .divide(previousClosePrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getIntraDayNetChange() {
        return (closePrice != null ? closePrice : BigDecimal.ZERO)
                .subtract(openPrice != null ? openPrice : BigDecimal.ZERO);
    }

    public BigDecimal getIntraDayNetChangePercentage() {
        if (openPrice == null || openPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // 시가가 없거나 0이면 비율을 계산할 수 없음
        }
        return getIntraDayNetChange()
                .divide(openPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public static TradeAssetStatus from(TradeAssetStatusEntity tradeAssetStatusEntity) {
        return TradeAssetStatus.builder()
                .tradeId(tradeAssetStatusEntity.getTradeId())
                .assetId(tradeAssetStatusEntity.getAssetId())
                .previousClosePrice(tradeAssetStatusEntity.getPreviousClosePrice())
                .openPrice(tradeAssetStatusEntity.getOpenPrice())
                .closePrice(tradeAssetStatusEntity.getClosePrice())
                .message(tradeAssetStatusEntity.getMessage())
                .build();
    }

}
