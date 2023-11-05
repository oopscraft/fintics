package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Ohlcv;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@Getter
public abstract class IndicatorResponse {

    @Builder.Default
    private List<Ohlcv> minuteOhlcvs = new ArrayList<>();

    @Builder.Default
    private List<Ohlcv> dailyOhlcvs = new ArrayList<>();

}
