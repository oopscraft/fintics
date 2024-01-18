package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.IndiceId;
import org.oopscraft.fintics.model.IndiceIndicator;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class IndiceIndicatorResponse extends IndicatorResponse {

    private IndiceId indiceId;

    private String indiceName;

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
