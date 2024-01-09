package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.awt.image.BandCombineOp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class Simulate {

    private final Trade trade;

    private LocalDateTime dateTimeFrom;

    private LocalDateTime dateTimeTo;







    private String holdCondition;

    private int interval;

    private LocalTime startAt;

    private LocalTime endAt;

    @Builder.Default
    private List<Ohlcv> minuteOhlcvs = new ArrayList<>();

    @Builder.Default
    private List<Ohlcv> dailyOhlcvs = new ArrayList<>();

    private Double feeRate;

    private Double bidAskSpread;


    private BigDecimal investAmount;

    @Builder.Default
    private Balance balance = new Balance();

    @Builder.Default
    private List<Order> orders = new ArrayList<>();

}
