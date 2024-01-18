package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetIndicator extends Indicator {

    private String assetId;

    private String assetName;

    @Override
    public String getIndicatorName() {
        return getAssetName();
    }

}
