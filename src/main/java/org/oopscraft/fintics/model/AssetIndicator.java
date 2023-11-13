package org.oopscraft.fintics.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class AssetIndicator extends Indicator {

    private final String symbol;

    private final String name;

    private final AssetType type;

}