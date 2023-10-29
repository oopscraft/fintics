package org.oopscraft.fintics.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Ohlcv {

    LocalDateTime dateTime;

    BigDecimal openPrice;

    BigDecimal highPrice;

    BigDecimal lowPrice;

    BigDecimal closePrice;

    BigDecimal volume;

}
