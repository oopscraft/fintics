package org.oopscraft.fintics.api.v1.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.IndiceIndicator;
import org.oopscraft.fintics.model.IndiceSymbol;

@SuperBuilder
@Getter
public class IndiceIndicatorResponse extends IndicatorResponse {

    private final IndiceSymbol symbol;

    private final String name;

    public static IndiceIndicatorResponse from(IndiceIndicator indiceIndicator) {
        return IndiceIndicatorResponse.builder()
                .symbol(indiceIndicator.getSymbol())
                .name(indiceIndicator.getSymbol().getValue())
                .minuteOhlcvs(indiceIndicator.getMinuteOhlcvs())
                .dailyOhlcvs(indiceIndicator.getDailyOhlcvs())
                .build();
    }

}
