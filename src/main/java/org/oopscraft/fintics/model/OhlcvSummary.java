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
public class OhlcvSummary implements Serializable {

    private String assetId;

    private String assetName;

    private Long dailyCount;

    private LocalDateTime dailyMinDateTime;

    private LocalDateTime dailyMaxDateTime;

    private Long minuteCount;

    private LocalDateTime minuteMinDateTime;

    private LocalDateTime minuteMaxDateTime;

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

    }

}
