package org.oopscraft.fintics.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class TradeAssetIndicator extends Indicator {

    private final String symbol;

    private final String name;

}