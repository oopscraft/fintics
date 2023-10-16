package org.oopscraft.fintics.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Ohlcv {

    LocalDateTime dateTime;

    Double openPrice;

    Double highPrice;

    Double lowPrice;

    Double closePrice;

    Double volume;

}
