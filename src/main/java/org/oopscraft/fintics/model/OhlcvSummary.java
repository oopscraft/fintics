package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetOhlcvSummary implements Serializable {

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
    private List<OhlcvStatistic> ohlcvStatistics = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OhlcvStatistic implements Serializable {
        private Instant date;
        private Long count;
    }

}
