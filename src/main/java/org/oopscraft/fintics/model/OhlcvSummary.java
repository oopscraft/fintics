package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class OhlcvSummary implements Serializable {

    private Long dailyCount;

    private Long dailyInterpolatedCount;

    private LocalDateTime dailyInterpolatedMaxDateTime;

    private Long minuteCount;

    private Long minuteInterpolatedCount;

    private LocalDateTime minuteInterpolatedMaxDateTime;

}
