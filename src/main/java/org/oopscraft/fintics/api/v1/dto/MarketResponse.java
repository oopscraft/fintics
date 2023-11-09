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

    private MarketIndicatorResponse kospiIndicator;

    private MarketIndicatorResponse usdKrwIndicator;

    public static MarketResponse from(Market market) {
        return MarketResponse.builder()
                .ndxIndicator(MarketIndicatorResponse.from(market.getNdxIndicator()))
                .ndxFutureIndicator(MarketIndicatorResponse.from(market.getNdxFutureIndicator()))
                .spxIndicator(MarketIndicatorResponse.from(market.getSpxIndicator()))
                .spxFutureIndicator(MarketIndicatorResponse.from(market.getSpxFutureIndicator()))
                .kospiIndicator(MarketIndicatorResponse.from(market.getKospiIndicator()))
                .usdKrwIndicator(MarketIndicatorResponse.from(market.getUsdKrwIndicator()))
                .build();
    }

}
