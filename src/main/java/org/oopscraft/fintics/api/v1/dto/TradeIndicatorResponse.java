package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.IndiceIndicator;

import java.util.List;

@Builder
@Getter
public class TradeIndicatorResponse {

    private List<AssetIndicatorResponse> assetIndicators;

    private List<IndiceIndicatorResponse> indiceIndicators;

}
