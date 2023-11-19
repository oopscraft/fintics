package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import net.bytebuddy.implementation.bind.annotation.Super;
import org.oopscraft.fintics.dao.OhlcvEntity;
import org.oopscraft.fintics.dao.TradeAssetOhlcvEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Ohlcv {

    private OhlcvType ohlcvType;

    private LocalDateTime dateTime;

    private BigDecimal openPrice;

    private BigDecimal highPrice;

    private BigDecimal lowPrice;

    private BigDecimal closePrice;

    private BigDecimal volume;

    public static Ohlcv from(OhlcvEntity OhlcvEntity) {
        return Ohlcv.builder()
                .ohlcvType(OhlcvEntity.getOhlcvType())
                .dateTime(OhlcvEntity.getDateTime())
                .openPrice(OhlcvEntity.getOpenPrice())
                .highPrice(OhlcvEntity.getHighPrice())
                .lowPrice(OhlcvEntity.getLowPrice())
                .closePrice(OhlcvEntity.getClosePrice())
                .volume(OhlcvEntity.getVolume())
                .build();
    }

}
