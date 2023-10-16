package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class SimulateTrade {

    private Trade trade;

    private List<SimulateTradeAsset> tradeAssetSimulates;

}
