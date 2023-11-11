package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Market {

    private IndiceIndicator ndxIndicator;

    private IndiceIndicator ndxFutureIndicator;

    private IndiceIndicator spxIndicator;

    private IndiceIndicator spxFutureIndicator;

    private IndiceIndicator kospiIndicator;

    private IndiceIndicator usdKrwIndicator;

}
