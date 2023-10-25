package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class Simulate {

    private String holdCondition;

    private Integer interval;

    @Builder.Default
    private List<Ohlcv> ohlcvs = new ArrayList<>();

    private Double feeRate;

    private Double bidAskSpread;

    @Builder.Default
    @Setter
    private List<Boolean> holdConditionResults = new ArrayList<>();

}
