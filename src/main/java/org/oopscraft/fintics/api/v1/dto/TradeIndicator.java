package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.MarketIndicator;

import java.util.List;

@Builder
@Getter
public class TradeIndicator {

    private List<AssetIndicatorResponse> assetIndicators;

    private MarketResponse market;

}
