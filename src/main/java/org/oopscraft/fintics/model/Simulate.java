package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.oopscraft.fintics.dao.SimulateEntity;

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

    @Setter
    private LocalDateTime startedAt;

    @Setter
    private LocalDateTime endedAt;

    @Setter
    @Builder.Default
    private Status status = Status.WAITING;

    private final Trade trade;

    private final LocalDateTime dateTimeFrom;

    private final LocalDateTime dateTimeTo;

    @Builder.Default
    private BigDecimal investAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal feeRate = BigDecimal.ZERO;

    private String result;

    @Builder.Default
    private List<IndiceIndicator> indiceIndicators = new ArrayList<>();

    @Builder.Default
    private List<AssetIndicator> assetIndicators = new ArrayList<>();

    @Builder.Default
    private Balance balance = new Balance();

    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    public static enum Status {
        WAITING, RUNNING, COMPLETED, FAILED
    }

    public static Simulate from(SimulateEntity simulateEntity) {
        return Simulate.builder()
                .simulateId(simulateEntity.getSimulateId())
                .status(simulateEntity.getStatus())
                .startedAt(simulateEntity.getStartedAt())
                .endedAt(simulateEntity.getEndedAt())
                .dateTimeFrom(simulateEntity.getDateTimeFrom())
                .dateTimeTo(simulateEntity.getDateTimeTo())
                .investAmount(simulateEntity.getInvestAmount())
                .feeRate(simulateEntity.getFeeRate())
                .build();
    }

}
