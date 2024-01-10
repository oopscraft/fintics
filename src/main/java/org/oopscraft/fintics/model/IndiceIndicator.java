package org.oopscraft.fintics.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class IndiceIndicator extends Indicator {

    private final IndiceId id;

    String getName() {
        return id.getValue();
    }

}
