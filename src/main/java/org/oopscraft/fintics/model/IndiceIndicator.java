package org.oopscraft.fintics.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class IndiceIndicator extends Indicator {

    private final IndiceId indiceId;

    String getIndiceName() {
        return indiceId.getValue();
    }

    @Override
    public String getIndicatorName() {
        return getIndiceName();
    }

}
