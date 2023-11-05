package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Indicator;
import org.oopscraft.fintics.model.MarketIndicator;
import org.oopscraft.fintics.model.Ohlcv;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@Getter
public class MarketIndicatorResponse extends IndicatorResponse {

    private final String name;

    public static MarketIndicatorResponse from(MarketIndicator marketIndicator) {
        return MarketIndicatorResponse.builder()
                .name(marketIndicator.getName())
                .minuteOhlcvs(marketIndicator.getMinuteOhlcvs())
                .dailyOhlcvs(marketIndicator.getDailyOhlcvs())
                .build();
    }

}
