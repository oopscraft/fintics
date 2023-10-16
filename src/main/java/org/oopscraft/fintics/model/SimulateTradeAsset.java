package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class SimulateTradeAsset {

    private TradeAsset tradeAsset;

    private List<Ohlcv> minuteOhlcvs;

    private List<Ohlcv> dailyOhlcvs;

    private List<AssetIndicator> assetIndicators;

}
