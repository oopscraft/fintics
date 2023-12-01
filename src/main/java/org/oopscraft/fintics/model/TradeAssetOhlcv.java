package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.TradeAssetOhlcvEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAssetOhlcv {

    private OhlcvType ohlcvType;

    private LocalDateTime dateTime;

    @Builder.Default
    private BigDecimal openPrice = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal highPrice = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal lowPrice = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal closePrice = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal volume = BigDecimal.ZERO;

    public static TradeAssetOhlcv of(LocalDateTime dateTime, double openPrice, double highPrice, double lowPrice, double closePrice, double volume) {
        return TradeAssetOhlcv.builder()
                .dateTime(dateTime)
                .openPrice(BigDecimal.valueOf(openPrice))
                .highPrice(BigDecimal.valueOf(highPrice))
                .lowPrice(BigDecimal.valueOf(lowPrice))
                .closePrice(BigDecimal.valueOf(closePrice))
                .volume(BigDecimal.valueOf(volume))
                .build();
    }

    public static TradeAssetOhlcv from(TradeAssetOhlcvEntity tradeAssetOhlcvEntity) {
        return TradeAssetOhlcv.builder()
                .ohlcvType(tradeAssetOhlcvEntity.getOhlcvType())
                .dateTime(tradeAssetOhlcvEntity.getDateTime())
                .openPrice(tradeAssetOhlcvEntity.getOpenPrice())
                .highPrice(tradeAssetOhlcvEntity.getHighPrice())
                .lowPrice(tradeAssetOhlcvEntity.getLowPrice())
                .closePrice(tradeAssetOhlcvEntity.getClosePrice())
                .volume(tradeAssetOhlcvEntity.getVolume())
                .build();
    }

}
