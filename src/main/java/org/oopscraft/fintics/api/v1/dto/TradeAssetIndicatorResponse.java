package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Indicator;
import org.oopscraft.fintics.model.Ohlcv;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@Getter
public class TradeAssetIndicatorResponse {

    private final String symbol;

    private final String name;

    @Builder.Default
    private List<Ohlcv> minuteOhlcvs = new ArrayList<>();

    @Builder.Default
    private List<Ohlcv> dailyOhlcvs = new ArrayList<>();

    public static TradeAssetIndicatorResponse from(Indicator tradeAssetIndicator) {
        return TradeAssetIndicatorResponse.builder()
                .symbol(tradeAssetIndicator.getSymbol())
                .name(tradeAssetIndicator.getName())
                .minuteOhlcvs(tradeAssetIndicator.getMinuteOhlcvs())
                .dailyOhlcvs(tradeAssetIndicator.getDailyOhlcvs())
                .build();
    }

}
