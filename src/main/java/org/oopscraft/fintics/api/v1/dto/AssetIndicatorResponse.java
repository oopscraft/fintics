package org.oopscraft.fintics.api.v1.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.AssetIndicator;

@SuperBuilder
@Getter
public class AssetIndicatorResponse extends IndicatorResponse {

    private final String id;

    private final String name;

    public static AssetIndicatorResponse from(AssetIndicator assetIndicator) {
        return AssetIndicatorResponse.builder()
                .id(assetIndicator.getId())
                .name(assetIndicator.getName())
                .minuteOhlcvs(assetIndicator.getMinuteOhlcvs())
                .dailyOhlcvs(assetIndicator.getDailyOhlcvs())
                .build();
    }

}
