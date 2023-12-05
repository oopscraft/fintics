package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.Indicator;
import org.oopscraft.fintics.model.Ohlcv;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@Getter
public class AssetIndicatorResponse extends IndicatorResponse {

    private final String symbol;

    private final String name;

    public static AssetIndicatorResponse from(AssetIndicator assetIndicator) {
        return AssetIndicatorResponse.builder()
                .symbol(assetIndicator.getSymbol())
                .name(assetIndicator.getName())
                .minuteOhlcvs(assetIndicator.getMinuteOhlcvs())
                .dailyOhlcvs(assetIndicator.getDailyOhlcvs())
                .build();
    }

}
