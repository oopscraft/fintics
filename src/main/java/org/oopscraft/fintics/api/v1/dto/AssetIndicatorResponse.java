package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.AssetIndicator;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetIndicatorResponse extends IndicatorResponse {

    private String assetId;

    private String assetName;

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
