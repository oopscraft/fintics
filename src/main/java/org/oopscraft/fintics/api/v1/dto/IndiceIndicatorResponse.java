package org.oopscraft.fintics.api.v1.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.IndiceId;
import org.oopscraft.fintics.model.IndiceIndicator;

@SuperBuilder
@Getter
public class IndiceIndicatorResponse extends IndicatorResponse {

    private final IndiceId indiceId;

    private final String indiceName;

    public static IndiceIndicatorResponse from(IndiceIndicator indiceIndicator) {
        return IndiceIndicatorResponse.builder()
                .indicatorName(indiceIndicator.getIndicatorName())
                .indiceId(indiceIndicator.getIndiceId())
                .indiceName(indiceIndicator.getIndiceId().getValue())
                .minuteOhlcvs(indiceIndicator.getMinuteOhlcvs())
                .dailyOhlcvs(indiceIndicator.getDailyOhlcvs())
                .build();
    }

}
