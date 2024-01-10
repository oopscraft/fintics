package org.oopscraft.fintics.api.v1.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.IndiceId;
import org.oopscraft.fintics.model.IndiceIndicator;

@SuperBuilder
@Getter
public class IndiceIndicatorResponse extends IndicatorResponse {

    private final IndiceId id;

    private final String name;

    public static IndiceIndicatorResponse from(IndiceIndicator indiceIndicator) {
        return IndiceIndicatorResponse.builder()
                .id(indiceIndicator.getId())
                .name(indiceIndicator.getId().getValue())
                .minuteOhlcvs(indiceIndicator.getMinuteOhlcvs())
                .dailyOhlcvs(indiceIndicator.getDailyOhlcvs())
                .build();
    }

}
