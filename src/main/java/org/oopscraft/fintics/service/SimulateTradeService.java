package org.oopscraft.fintics.service;

import org.oopscraft.fintics.client.Client;
import org.oopscraft.fintics.client.ClientFactory;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.thread.TradeAssetDecider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimulateTradeService {

    public SimulateTrade simulateTrade(Trade trade) {
        Client client = ClientFactory.getClient(trade.getClientType(), trade.getClientProperties());
        List<SimulateTradeAsset> tradeAssetSimulates = new ArrayList<>();
        for(TradeAsset tradeAsset : trade.getTradeAssets()) {
            SimulateTradeAsset tradeAssetSimulate = simulateTradeAsset(trade, tradeAsset, client);
            tradeAssetSimulates.add(tradeAssetSimulate);
        }
        return SimulateTrade.builder()
                .trade(trade)
                .tradeAssetSimulates(tradeAssetSimulates)
                .build();
    }

    private SimulateTradeAsset simulateTradeAsset(Trade trade, TradeAsset tradeAsset, Client client) {
        List<Ohlcv> minuteOhlcvs = client.getMinuteOhlcvs(tradeAsset);
        List<Ohlcv> dailyOhlcvs = client.getDailyOhlcvs(tradeAsset);
        List<AssetIndicator> assetIndicators = new ArrayList<>();

        for(int i = 0; i < minuteOhlcvs.size(); i ++) {
            AssetIndicator assetIndicator = AssetIndicator.builder()
                    .asset(tradeAsset)
                    .minuteOhlcvs(minuteOhlcvs.subList(0, i))
                    .dailyOhlcvs(dailyOhlcvs)
                    .build();

            TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                    .holdCondition(trade.getHoldCondition())
                    .assetIndicator(assetIndicator)
                    .build();

            Boolean holdConditionResult = tradeAssetDecider.execute();
            assetIndicator.setHoldConditionResult(holdConditionResult);
            assetIndicators.add(assetIndicator);
        }

        return SimulateTradeAsset.builder()
                .tradeAsset(tradeAsset)
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .assetIndicators(assetIndicators)
                .build();
    }

}
