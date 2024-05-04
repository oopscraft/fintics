package org.oopscraft.fintics.api.v1.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class OhlcvSummaryResponse {

    private Long dailyCount;

    private Long dailyInterpolatedCount;

    private LocalDateTime dailyInterpolatedMaxDateTime;

    private Long minuteCount;

    private Long minuteInterpolatedCount;

    private LocalDateTime minuteInterpolatedMaxDateTime;

}
