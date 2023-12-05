package org.oopscraft.fintics.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class IndiceIndicator extends Indicator {

    private final IndiceSymbol symbol;

    String getName() {
        return symbol.getValue();
    }

}
