package org.oopscraft.fintics.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class AssetIndicator extends Indicator {

    private final String assetId;

    private final String assetName;

    @Override
    public String getIndicatorName() {
        return getAssetName();
    }

}
