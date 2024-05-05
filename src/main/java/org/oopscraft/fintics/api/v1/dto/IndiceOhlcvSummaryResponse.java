package org.oopscraft.fintics.api.v1.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.IndiceOhlcvSummary;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Getter
public class IndiceOhlcvSummaryResponse extends OhlcvSummaryResponse {

    private Indice.Id indiceId;

    private String indiceName;

    public static IndiceOhlcvSummaryResponse from(IndiceOhlcvSummary indiceOhlcvSummary) {
        return IndiceOhlcvSummaryResponse.builder()
                .indiceId(indiceOhlcvSummary.getIndiceId())
                .indiceName(indiceOhlcvSummary.getIndiceName())
                .dailyCount(indiceOhlcvSummary.getDailyCount())
                .dailyInterpolatedCount(indiceOhlcvSummary.getDailyInterpolatedCount())
                .dailyInterpolatedMaxDateTime(indiceOhlcvSummary.getDailyInterpolatedMaxDateTime())
                .minuteCount(indiceOhlcvSummary.getMinuteCount())
                .minuteInterpolatedCount(indiceOhlcvSummary.getMinuteInterpolatedCount())
                .minuteInterpolatedMaxDateTime(indiceOhlcvSummary.getMinuteInterpolatedMaxDateTime())
                .ohlcvStatistics(indiceOhlcvSummary.getOhlcvStatistics().stream().map(OhlcvStatisticResponse::from).toList())
                .build();
    }

}
