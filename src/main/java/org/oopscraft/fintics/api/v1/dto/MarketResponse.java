package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Market;

@Builder
@Getter
public class MarketResponse {

    private IndiceIndicatorResponse ndxIndicator;

    private IndiceIndicatorResponse ndxFutureIndicator;

    private IndiceIndicatorResponse spxIndicator;

    private IndiceIndicatorResponse spxFutureIndicator;

    private IndiceIndicatorResponse kospiIndicator;

    private IndiceIndicatorResponse usdKrwIndicator;

    public static MarketResponse from(Market market) {
        return MarketResponse.builder()
                .ndxIndicator(IndiceIndicatorResponse.from(market.getNdxIndicator()))
                .ndxFutureIndicator(IndiceIndicatorResponse.from(market.getNdxFutureIndicator()))
                .spxIndicator(IndiceIndicatorResponse.from(market.getSpxIndicator()))
                .spxFutureIndicator(IndiceIndicatorResponse.from(market.getSpxFutureIndicator()))
                .kospiIndicator(IndiceIndicatorResponse.from(market.getKospiIndicator()))
                .usdKrwIndicator(IndiceIndicatorResponse.from(market.getUsdKrwIndicator()))
                .build();
    }

}
