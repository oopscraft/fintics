package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.calculator.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuperBuilder
@Slf4j
public abstract class Indicator {

    @Builder.Default
    @Getter
    private final List<Ohlcv> minuteOhlcvs = new ArrayList<>();

    @Builder.Default
    @Getter
    private final List<Ohlcv> dailyOhlcvs = new ArrayList<>();

}