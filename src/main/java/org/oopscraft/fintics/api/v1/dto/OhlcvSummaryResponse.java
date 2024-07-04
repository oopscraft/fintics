package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.OhlcvSummary;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetOhlcvSummaryResponse {

    private String id;

    private String name;

    private Long dailyCount;

    private Instant dailyMinDatetime;

    private Instant dailyMaxDatetime;

    private Long minuteCount;

    private Instant minuteMinDatetime;

    private Instant minuteMaxDatetime;

    @Builder.Default
    @Setter
    private List<OhlcvStatisticResponse> ohlcvStatistics = new ArrayList<>();

    public static AssetOhlcvSummaryResponse from(OhlcvSummary ohlcvSummary) {
        return AssetOhlcvSummaryResponse.builder()
                .id(ohlcvSummary.getId())
                .name(ohlcvSummary.getName())
                .dailyCount(ohlcvSummary.getDailyCount())
                .dailyMinDatetime(ohlcvSummary.getDailyMinDatetime())
                .dailyMaxDatetime(ohlcvSummary.getDailyMaxDatetime())
                .minuteCount(ohlcvSummary.getMinuteCount())
                .minuteMinDatetime(ohlcvSummary.getMinuteMinDatetime())
                .minuteMaxDatetime(ohlcvSummary.getMinuteMaxDatetime())
                .ohlcvStatistics(ohlcvSummary.getOhlcvStatistics().stream()
                        .map(OhlcvStatisticResponse::from)
                        .toList())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OhlcvStatisticResponse {
        private Instant date;
        private Long count;

        public static OhlcvStatisticResponse from(OhlcvSummary.OhlcvStatistic ohlcvStatistic) {
            return OhlcvStatisticResponse.builder()
                    .date(ohlcvStatistic.getDate())
                    .count(ohlcvStatistic.getCount())
                    .build();
        }

    }

}
