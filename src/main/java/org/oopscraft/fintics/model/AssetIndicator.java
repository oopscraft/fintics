package org.oopscraft.fintics.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.calculator.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuperBuilder
@Getter
public class AssetIndicator extends Indicator {

    private final String symbol;

    private final String name;

    private final AssetType type;

}