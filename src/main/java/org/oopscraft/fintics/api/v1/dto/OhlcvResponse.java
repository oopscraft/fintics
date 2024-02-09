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
public abstract class OhlcvResponse {

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

}
