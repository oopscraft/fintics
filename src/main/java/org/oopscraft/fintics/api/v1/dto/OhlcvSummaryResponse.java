package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.OhlcvSummary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OhlcvSummaryResponse {

    private String id;

    private String name;

    private Long dailyCount;

    private LocalDateTime dailyMinDateTime;

    private LocalDateTime dailyMaxDateTime;

    private Long minuteCount;

    private LocalDateTime minuteMinDateTime;

    private LocalDateTime minuteMaxDateTime;

    @Builder.Default
    @Setter
    private List<OhlcvStatisticResponse> ohlcvStatistics = new ArrayList<>();

    public static OhlcvSummaryResponse from(OhlcvSummary ohlcvSummary) {
        return OhlcvSummaryResponse.builder()
                .id(ohlcvSummary.getId())
                .name(ohlcvSummary.getName())
                .dailyCount(ohlcvSummary.getDailyCount())
                .dailyMinDateTime(ohlcvSummary.getDailyMinDateTime())
                .dailyMaxDateTime(ohlcvSummary.getDailyMaxDateTime())
                .minuteCount(ohlcvSummary.getMinuteCount())
                .minuteMinDateTime(ohlcvSummary.getMinuteMinDateTime())
                .minuteMaxDateTime(ohlcvSummary.getMinuteMaxDateTime())
                .ohlcvStatistics(ohlcvSummary.getOhlcvStatistics().stream().map(OhlcvStatisticResponse::from).toList())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OhlcvStatisticResponse {
        private LocalDate date;
        private Long count;

        public static OhlcvStatisticResponse from(OhlcvSummary.OhlcvStatistic ohlcvStatistic) {
            return OhlcvStatisticResponse.builder()
                    .date(ohlcvStatistic.getDate())
                    .count(ohlcvStatistic.getCount())
                    .build();
        }

    }

}
