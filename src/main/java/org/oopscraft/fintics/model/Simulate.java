package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class Simulate {

    private String holdCondition;

    private Integer interval;

    private LocalTime startAt;

    private LocalTime endAt;

    @Builder.Default
    private List<Ohlcv> minuteOhlcvs = new ArrayList<>();

    @Builder.Default
    private List<Ohlcv> dailyOhlcvs = new ArrayList<>();

    private Double feeRate;

    private Double bidAskSpread;

    @Builder.Default
    @Setter
    private List<Boolean> holdConditionResults = new ArrayList<>();

}
