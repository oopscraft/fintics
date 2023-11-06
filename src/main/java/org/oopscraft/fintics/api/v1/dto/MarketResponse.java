package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Market;
import org.oopscraft.fintics.model.MarketIndicator;

@Builder
@Getter
public class MarketResponse {

    private MarketIndicatorResponse ndxIndicator;

    private MarketIndicatorResponse ndxFutureIndicator;

    private MarketIndicatorResponse spxIndicator;

    private MarketIndicatorResponse spxFutureIndicator;

    private MarketIndicatorResponse djiIndicator;

    private MarketIndicatorResponse djiFutureIndicator;

    public static MarketResponse from(Market market) {
        return MarketResponse.builder()
                .ndxIndicator(MarketIndicatorResponse.from(market.getNdxIndicator()))
                .ndxFutureIndicator(MarketIndicatorResponse.from(market.getNdxFutureIndicator()))
                .spxIndicator(MarketIndicatorResponse.from(market.getSpxIndicator()))
                .spxFutureIndicator(MarketIndicatorResponse.from(market.getSpxFutureIndicator()))
                .djiIndicator(MarketIndicatorResponse.from(market.getDjiIndicator()))
                .djiFutureIndicator(MarketIndicatorResponse.from(market.getDjiFutureIndicator()))
                .build();
    }

}
