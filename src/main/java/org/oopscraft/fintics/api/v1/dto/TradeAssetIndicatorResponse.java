package org.oopscraft.fintics.api.v1.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.TradeAssetIndicator;

@SuperBuilder
@Getter
public class TradeAssetIndicatorResponse extends IndicatorResponse {

    private final String symbol;

    private final String name;

    public static TradeAssetIndicatorResponse from(TradeAssetIndicator tradeAssetIndicator) {
        return TradeAssetIndicatorResponse.builder()
                .symbol(tradeAssetIndicator.getSymbol())
                .name(tradeAssetIndicator.getName())
                .minuteOhlcvs(tradeAssetIndicator.getMinuteOhlcvs())
                .dailyOhlcvs(tradeAssetIndicator.getDailyOhlcvs())
                .build();
    }

}
