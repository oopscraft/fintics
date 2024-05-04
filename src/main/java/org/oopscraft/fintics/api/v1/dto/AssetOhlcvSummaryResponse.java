package org.oopscraft.fintics.api.v1.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.AssetOhlcvSummary;

import java.io.Serializable;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Getter
public class AssetOhlcvSummaryResponse extends OhlcvSummaryResponse implements Serializable {

    private String assetId;

    private String assetName;

    private Integer tradeCount;

    public static AssetOhlcvSummaryResponse from(AssetOhlcvSummary assetOhlcvSummary) {
        return AssetOhlcvSummaryResponse.builder()
                .assetId(assetOhlcvSummary.getAssetId())
                .assetName(assetOhlcvSummary.getAssetName())
                .tradeCount(assetOhlcvSummary.getTradeCount())
                .dailyCount(assetOhlcvSummary.getDailyCount())
                .dailyInterpolatedCount(assetOhlcvSummary.getDailyInterpolatedCount())
                .dailyInterpolatedMaxDateTime(assetOhlcvSummary.getDailyInterpolatedMaxDateTime())
                .minuteCount(assetOhlcvSummary.getMinuteCount())
                .minuteInterpolatedCount(assetOhlcvSummary.getMinuteInterpolatedCount())
                .minuteInterpolatedMaxDateTime(assetOhlcvSummary.getMinuteInterpolatedMaxDateTime())
                .build();
    }

}
