package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Deprecated
public class Simulate {

    private Trade trade;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Double feeRate;

    private Double bidAskSpread;

}
