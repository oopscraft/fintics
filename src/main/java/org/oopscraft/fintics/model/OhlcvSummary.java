package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Builder.Default
    @Setter
    private List<OhlcvStatistic> ohlcvStatistics = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OhlcvStatistic implements Serializable {
        private LocalDate date;
        private Long count;
        private Long interpolatedCount;
    }

}
