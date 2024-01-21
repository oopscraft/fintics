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

    private final String simulateId;

    private final Trade trade;

    private final LocalDateTime dateTimeFrom;

    private final LocalDateTime dateTimeTo;

    @Builder.Default
    private List<IndiceIndicator> indiceIndicators = new ArrayList<>();

    @Builder.Default
    private List<AssetIndicator> assetIndicators = new ArrayList<>();

    @Builder.Default
    private BigDecimal investAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal feeRate = BigDecimal.ZERO;

    @Builder.Default
    private Balance balance = new Balance();

    @Builder.Default
    private List<Order> orders = new ArrayList<>();

}
