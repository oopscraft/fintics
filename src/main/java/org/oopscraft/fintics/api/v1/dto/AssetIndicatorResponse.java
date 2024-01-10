package org.oopscraft.fintics.api.v1.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.AssetIndicator;

@SuperBuilder
@Getter
public class AssetIndicatorResponse extends IndicatorResponse {

    private final String assetId;

    private final String assetName;

    public static AssetIndicatorResponse from(AssetIndicator assetIndicator) {
        return AssetIndicatorResponse.builder()
                .indicatorName(assetIndicator.getIndicatorName())
                .assetId(assetIndicator.getAssetId())
                .assetName(assetIndicator.getAssetName())
                .minuteOhlcvs(assetIndicator.getMinuteOhlcvs())
                .dailyOhlcvs(assetIndicator.getDailyOhlcvs())
                .build();
    }


}
