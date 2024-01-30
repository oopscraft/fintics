package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AssetSearch {

    private String assetId;

    private String assetName;

}
