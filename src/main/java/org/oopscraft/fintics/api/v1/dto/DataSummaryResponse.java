package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.DataSummary;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Ohlcv;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DataSummaryResponse {

    @Builder.Default
    private List<AssetOhlcvStatisticsResponse> assetMinuteOhlcvStatistics = new ArrayList<>();

    @Builder.Default
    private List<AssetOhlcvStatisticsResponse> assetDailyOhlcvStatistics = new ArrayList<>();

    @Builder.Default
    private List<IndiceOhlcvStatisticsResponse> indiceMinuteOhlcvStatistics = new ArrayList<>();

    @Builder.Default
    private List<IndiceOhlcvStatisticsResponse> indiceDailyOhlcvStatistics = new ArrayList<>();

    public static DataSummaryResponse from(DataSummary dataSummary) {
        return DataSummaryResponse.builder()
                .assetMinuteOhlcvStatistics(dataSummary.getAssetMinuteOhlcvStatistics().stream()
                        .map(AssetOhlcvStatisticsResponse::from).toList())
                .assetDailyOhlcvStatistics(dataSummary.getAssetDailyOhlcvStatistics().stream()
                        .map(AssetOhlcvStatisticsResponse::from).toList())
                .indiceMinuteOhlcvStatistics(dataSummary.getIndiceMinuteOhlcvStatistics().stream()
                        .map(IndiceOhlcvStatisticsResponse::from).toList())
                .indiceDailyOhlcvStatistics(dataSummary.getIndiceDailyOhlcvStatistics().stream()
                        .map(IndiceOhlcvStatisticsResponse::from).toList())
                .build();
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public abstract static class OhlcvStatisticsResponse {
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
    public static class AssetOhlcvStatisticsResponse extends OhlcvStatisticsResponse {
        private String assetId;
        private String assetName;
        private boolean usedByTrade;

        public static AssetOhlcvStatisticsResponse from(DataSummary.AssetOhlcvStatistics assetOhlcvStatistics) {
            return AssetOhlcvStatisticsResponse.builder()
                    .assetId(assetOhlcvStatistics.getAssetId())
                    .assetName(assetOhlcvStatistics.getAssetName())
                    .type(assetOhlcvStatistics.getType())
                    .minDateTime(assetOhlcvStatistics.getMinDateTime())
                    .maxDateTime(assetOhlcvStatistics.getMaxDateTime())
                    .totalCount(assetOhlcvStatistics.getTotalCount())
                    .interpolatedMinDateTime(assetOhlcvStatistics.getInterpolatedMinDateTime())
                    .interpolatedMaxDateTime(assetOhlcvStatistics.getInterpolatedMaxDateTime())
                    .interpolatedCount(assetOhlcvStatistics.getInterpolatedCount())
                    .usedByTrade(assetOhlcvStatistics.isUsedByTrade())
                    .build();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class IndiceOhlcvStatisticsResponse extends OhlcvStatisticsResponse {

        private String indiceId;

        public static IndiceOhlcvStatisticsResponse from(DataSummary.IndiceOhlcvStatistics indiceOhlcvStatistics) {
            return IndiceOhlcvStatisticsResponse.builder()
                    .indiceId(indiceOhlcvStatistics.getIndiceId())
                    .type(indiceOhlcvStatistics.getType())
                    .minDateTime(indiceOhlcvStatistics.getMinDateTime())
                    .maxDateTime(indiceOhlcvStatistics.getMaxDateTime())
                    .totalCount(indiceOhlcvStatistics.getTotalCount())
                    .interpolatedMinDateTime(indiceOhlcvStatistics.getInterpolatedMinDateTime())
                    .interpolatedMaxDateTime(indiceOhlcvStatistics.getInterpolatedMaxDateTime())
                    .interpolatedCount(indiceOhlcvStatistics.getInterpolatedCount())
                    .build();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @SuperBuilder
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class IndiceOhlcvResponse extends OhlcvResponse {

        private Indice.Id indiceId;

        public static IndiceOhlcvResponse from(DataSummary.IndiceOhlcv indiceOhlcv) {
            return IndiceOhlcvResponse.builder()
                    .indiceId(indiceOhlcv.getIndiceId())
                    .type(indiceOhlcv.getType())
                    .dateTime(indiceOhlcv.getDateTime())
                    .openPrice(indiceOhlcv.getOpenPrice())
                    .highPrice(indiceOhlcv.getOpenPrice())
                    .lowPrice(indiceOhlcv.getLowPrice())
                    .closePrice(indiceOhlcv.getClosePrice())
                    .volume(indiceOhlcv.getVolume())
                    .interpolated(indiceOhlcv.isInterpolated())
                    .build();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @SuperBuilder
    @AllArgsConstructor
    public static class AssetOhlcvResponse extends OhlcvResponse {

        private String brokerId;

        private String assetId;

        public static AssetOhlcvResponse from(DataSummary.AssetOhlcv assetOhlcv) {
            return AssetOhlcvResponse.builder()
                    .assetId(assetOhlcv.getAssetId())
                    .type(assetOhlcv.getType())
                    .dateTime(assetOhlcv.getDateTime())
                    .openPrice(assetOhlcv.getOpenPrice())
                    .highPrice(assetOhlcv.getOpenPrice())
                    .lowPrice(assetOhlcv.getLowPrice())
                    .closePrice(assetOhlcv.getClosePrice())
                    .volume(assetOhlcv.getVolume())
                    .interpolated(assetOhlcv.isInterpolated())
                    .build();
        }
    }

}
