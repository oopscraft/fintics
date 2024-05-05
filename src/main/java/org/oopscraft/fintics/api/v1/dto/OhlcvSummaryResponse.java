package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.OhlcvSummary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Builder.Default
    @Setter
    private List<OhlcvStatisticResponse> ohlcvStatistics = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OhlcvStatisticResponse {
        private LocalDate date;
        private Long count;
        private Long interpolatedCount;

        public static OhlcvStatisticResponse from(OhlcvSummary.OhlcvStatistic ohlcvStatistic) {
            return OhlcvStatisticResponse.builder()
                    .date(ohlcvStatistic.getDate())
                    .count(ohlcvStatistic.getCount())
                    .interpolatedCount(ohlcvStatistic.getInterpolatedCount())
                    .build();
        }

    }

}
