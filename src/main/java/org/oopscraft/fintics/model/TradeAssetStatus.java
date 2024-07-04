package org.oopscraft.fintics.model;

import lombok.*;
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

    private BigDecimal previousClose;

    private BigDecimal open;

    private BigDecimal close;

    private String message;

    public BigDecimal getNetChange() {
        return (close != null ? close : BigDecimal.ZERO)
                .subtract(previousClose != null ? previousClose : BigDecimal.ZERO);
    }

    public BigDecimal getNetChangePercentage() {
        if (previousClose == null || previousClose.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // 이전 종가가 없거나 0이면 비율을 계산할 수 없음
        }
        return getNetChange()
                .divide(previousClose, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getIntraDayNetChange() {
        return (close != null ? close : BigDecimal.ZERO)
                .subtract(open != null ? open : BigDecimal.ZERO);
    }

    public BigDecimal getIntraDayNetChangePercentage() {
        if (open == null || open.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // 시가가 없거나 0이면 비율을 계산할 수 없음
        }
        return getIntraDayNetChange()
                .divide(open, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public static TradeAssetStatus from(TradeAssetStatusEntity tradeAssetStatusEntity) {
        return TradeAssetStatus.builder()
                .tradeId(tradeAssetStatusEntity.getTradeId())
                .assetId(tradeAssetStatusEntity.getAssetId())
                .previousClose(tradeAssetStatusEntity.getPreviousClose())
                .open(tradeAssetStatusEntity.getOpen())
                .close(tradeAssetStatusEntity.getClose())
                .message(tradeAssetStatusEntity.getMessage())
                .build();
    }

}
