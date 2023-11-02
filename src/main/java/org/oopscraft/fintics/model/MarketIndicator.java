package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.bytebuddy.implementation.bind.annotation.Super;

import java.util.List;

@SuperBuilder
@Getter
public class MarketIndicator extends Indicator {

    private final String name;

}
