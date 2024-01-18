package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Ohlcv;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class IndicatorResponse {

    public String indicatorName;

    @Builder.Default
    private List<Ohlcv> minuteOhlcvs = new ArrayList<>();

    @Builder.Default
    private List<Ohlcv> dailyOhlcvs = new ArrayList<>();

}
