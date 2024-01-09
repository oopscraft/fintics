package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.OhlcvEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Ohlcv {

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

    public static Ohlcv of(LocalDateTime dateTime, double openPrice, double highPrice, double lowPrice, double closePrice, double volume) {
        return Ohlcv.builder()
                .dateTime(dateTime)
                .openPrice(BigDecimal.valueOf(openPrice))
                .highPrice(BigDecimal.valueOf(highPrice))
                .lowPrice(BigDecimal.valueOf(lowPrice))
                .closePrice(BigDecimal.valueOf(closePrice))
                .volume(BigDecimal.valueOf(volume))
                .build();
    }

    public static Ohlcv from(OhlcvEntity ohlcvEntity) {
        return Ohlcv.builder()
                .ohlcvType(ohlcvEntity.getOhlcvType())
                .dateTime(ohlcvEntity.getDateTime())
                .openPrice(ohlcvEntity.getOpenPrice())
                .highPrice(ohlcvEntity.getHighPrice())
                .lowPrice(ohlcvEntity.getLowPrice())
                .closePrice(ohlcvEntity.getClosePrice())
                .volume(ohlcvEntity.getVolume())
                .build();
    }

}
