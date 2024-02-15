package org.oopscraft.fintics.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.arch4j.core.support.ObjectMapperHolder;
import org.oopscraft.fintics.dao.SimulateEntity;

import javax.persistence.Converter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class Simulate {

    @Setter
    private String simulateId;

    @Setter
    private LocalDateTime startedAt;

    @Setter
    private LocalDateTime endedAt;

    @Setter
    @Builder.Default
    private Status status = Status.WAITING;

    private String tradeId;

    private String tradeName;

    private final Trade trade;

    private final LocalDateTime dateTimeFrom;

    private final LocalDateTime dateTimeTo;

    @Builder.Default
    private BigDecimal investAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal feeRate = BigDecimal.ZERO;

    private String result;

    @Builder.Default
    private Balance balance = new Balance();

    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    public enum Status { WAITING, RUNNING, COMPLETED, STOPPING, STOPPED, FAILED }

    @Converter(autoApply = true)
    public static class StatusConverter extends AbstractEnumConverter<Status> {}

    public static Simulate from(SimulateEntity simulateEntity) {
        ObjectMapper objectMapper = ObjectMapperHolder.getObject();

        // trade data
        Trade trade = null;
        if(simulateEntity.getTradeData() != null) {
            try {
                trade = objectMapper.readValue(simulateEntity.getTradeData(), Trade.class);
            } catch (JsonProcessingException ignored) {
                trade = new Trade();
            }
        }

        // balance data
        Balance balance = null;
        if(simulateEntity.getBalanceData() != null) {
            try {
                balance = objectMapper.readValue(simulateEntity.getBalanceData(), Balance.class);
            } catch (JsonProcessingException ignored) {
                balance = new Balance();
            }
        }

        // orders data
        List<Order> orders = null;
        if(simulateEntity.getOrdersData() != null) {
            try {
                orders = objectMapper.readValue(simulateEntity.getOrdersData(), new TypeReference<>(){});
            } catch (JsonProcessingException ignored) {
                orders = new ArrayList<>();
            }
        }

        // return
        return Simulate.builder()
                .simulateId(simulateEntity.getSimulateId())
                .status(simulateEntity.getStatus())
                .startedAt(simulateEntity.getStartedAt())
                .endedAt(simulateEntity.getEndedAt())
                .tradeId(simulateEntity.getTradeId())
                .tradeName(simulateEntity.getTradeName())
                .trade(trade)
                .dateTimeFrom(simulateEntity.getDateTimeFrom())
                .dateTimeTo(simulateEntity.getDateTimeTo())
                .investAmount(simulateEntity.getInvestAmount())
                .feeRate(simulateEntity.getFeeRate())
                .balance(balance)
                .orders(orders)
                .build();
    }

}
