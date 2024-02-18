package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DataSummary {

    @Builder.Default
    private List<AssetOhlcvStatistics> assetMinuteOhlcvStatistics = new ArrayList<>();

    @Builder.Default
    private List<AssetOhlcvStatistics> assetDailyOhlcvStatistics = new ArrayList<>();

    @Builder.Default
    private List<IndiceOhlcvStatistics> indiceMinuteOhlcvStatistics = new ArrayList<>();

    @Builder.Default
    private List<IndiceOhlcvStatistics> indiceDailyOhlcvStatistics = new ArrayList<>();

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public abstract static class OhlcvStatistics implements Serializable {
        private Ohlcv.Type type;
        private LocalDateTime minDateTime;
        private LocalDateTime maxDateTime;
        private Long totalCount;
        private LocalDateTime interpolatedMinDateTime;
        private LocalDateTime interpolatedMaxDateTime;
        private Long interpolatedCount;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AssetOhlcvStatistics extends OhlcvStatistics implements Serializable {
        private String assetId;
        private String assetName;
        private boolean usedByTrade;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class IndiceOhlcvStatistics extends OhlcvStatistics implements Serializable {
        private IndiceId indiceId;
    }

}
