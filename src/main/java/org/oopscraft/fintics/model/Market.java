package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Market {

    private MarketIndex spx;

    private MarketIndex spxFuture;

    private MarketIndex dji;

    private MarketIndex djiFuture;

    private MarketIndex ndx;

    private MarketIndex ndxFuture;

}
