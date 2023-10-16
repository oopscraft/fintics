package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import org.oopscraft.fintics.model.SimulateTrade;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.SimulateTradeAsset;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public class SimulateTradeResponse {

    private Trade trade;

    private List<SimulateTradeAssetResponse> simulateTradeAssets;

    public static SimulateTradeResponse from(SimulateTrade simulateTrade) {
        return SimulateTradeResponse.builder()
                .simulateTradeAssets(simulateTrade.getTradeAssetSimulates().stream()
                        .map(SimulateTradeAssetResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }

}
