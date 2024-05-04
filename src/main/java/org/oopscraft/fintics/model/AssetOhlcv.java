package org.oopscraft.fintics.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.AssetOhlcvEntity;

@SuperBuilder
@Getter
public class AssetOhlcv extends Ohlcv {

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
