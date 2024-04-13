package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.AssetOhlcvEntity;
import org.oopscraft.fintics.dao.IndiceOhlcvEntity;

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
        private String indiceId;
    }

    @SuperBuilder
    @Getter
    public static class AssetOhlcv extends Ohlcv {
        private String assetId;
        public static AssetOhlcv from(AssetOhlcvEntity assetOhlcvEntity) {
            return AssetOhlcv.builder()
                    .assetId(assetOhlcvEntity.getAssetId())
                    .type(assetOhlcvEntity.getType())
                    .dateTime(assetOhlcvEntity.getDateTime())
                    .openPrice(assetOhlcvEntity.getOpenPrice())
                    .highPrice(assetOhlcvEntity.getHighPrice())
                    .lowPrice(assetOhlcvEntity.getLowPrice())
                    .closePrice(assetOhlcvEntity.getClosePrice())
                    .volume(assetOhlcvEntity.getVolume())
                    .interpolated(assetOhlcvEntity.isInterpolated())
                    .build();
        }
    }

    @SuperBuilder
    @Getter
    public static class IndiceOhlcv extends Ohlcv {
        private Indice.Id indiceId;
        public static IndiceOhlcv from(IndiceOhlcvEntity indiceOhlcvEntity) {
            return IndiceOhlcv.builder()
                    .indiceId(indiceOhlcvEntity.getIndiceId())
                    .type(indiceOhlcvEntity.getType())
                    .dateTime(indiceOhlcvEntity.getDateTime())
                    .openPrice(indiceOhlcvEntity.getOpenPrice())
                    .highPrice(indiceOhlcvEntity.getHighPrice())
                    .lowPrice(indiceOhlcvEntity.getLowPrice())
                    .closePrice(indiceOhlcvEntity.getClosePrice())
                    .volume(indiceOhlcvEntity.getVolume())
                    .interpolated(indiceOhlcvEntity.isInterpolated())
                    .build();
        }
    }

}
