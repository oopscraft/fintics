package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.SimulateTradeAsset;
import org.oopscraft.fintics.model.TradeAsset;

import java.util.List;

@Builder
public class SimulateTradeAssetResponse {

    private TradeAsset tradeAsset;

    private List<Ohlcv> minuteOhlcvs;

    private List<Ohlcv> dailyOhlcvs;

    private List<AssetIndicator> assetIndicators;

    public static SimulateTradeAssetResponse from(SimulateTradeAsset simulateTradeAsset) {
        return SimulateTradeAssetResponse.builder()
                .tradeAsset(simulateTradeAsset.getTradeAsset())
                .minuteOhlcvs(simulateTradeAsset.getMinuteOhlcvs())
                .dailyOhlcvs(simulateTradeAsset.getDailyOhlcvs())
                .assetIndicators(simulateTradeAsset.getAssetIndicators())
                .build();
    }

}
