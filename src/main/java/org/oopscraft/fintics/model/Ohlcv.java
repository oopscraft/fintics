package org.oopscraft.fintics.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Ohlcv {

    private LocalDateTime dateTime;

    private BigDecimal openPrice;

    private BigDecimal highPrice;

    private BigDecimal lowPrice;

    private BigDecimal closePrice;

    private BigDecimal volume;

}
