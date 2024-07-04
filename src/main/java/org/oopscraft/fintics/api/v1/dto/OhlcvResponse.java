package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetOhlcvResponse {

    private Ohlcv.Type type;

    private Instant datetime;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private BigDecimal volume;

    public static AssetOhlcvResponse from(Ohlcv ohlcv) {
        return AssetOhlcvResponse.builder()
                .datetime(ohlcv.getDatetime())
                .type(ohlcv.getType())
                .open(ohlcv.getOpen())
                .high(ohlcv.getHigh())
                .low(ohlcv.getLow())
                .close(ohlcv.getClose())
                .volume(ohlcv.getVolume())
                .build();
    }

}
