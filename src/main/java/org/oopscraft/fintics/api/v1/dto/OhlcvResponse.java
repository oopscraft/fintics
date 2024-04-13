package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class OhlcvResponse {

    private Ohlcv.Type type;

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

    @Builder.Default
    private boolean interpolated = false;

    public static OhlcvResponse from(Ohlcv ohlcv) {
        return OhlcvResponse.builder()
                .dateTime(ohlcv.getDateTime())
                .type(ohlcv.getType())
                .openPrice(ohlcv.getOpenPrice())
                .highPrice(ohlcv.getHighPrice())
                .lowPrice(ohlcv.getLowPrice())
                .closePrice(ohlcv.getClosePrice())
                .volume(ohlcv.getVolume())
                .interpolated(ohlcv.isInterpolated())
                .build();
    }

}
