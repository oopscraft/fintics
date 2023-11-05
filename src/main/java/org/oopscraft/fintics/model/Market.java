package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Market {

    private MarketIndicator ndxIndicator;

    private MarketIndicator ndxFutureIndicator;

    private MarketIndicator spxIndicator;

    private MarketIndicator spxFutureIndicator;

    private MarketIndicator djiIndicator;

    private MarketIndicator djiFutureIndicator;

}
