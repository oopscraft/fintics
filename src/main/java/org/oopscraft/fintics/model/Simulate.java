package org.oopscraft.fintics.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

    private final Strategy strategy;

    private final LocalDateTime dateTimeFrom;

    private final LocalDateTime dateTimeTo;

    private BigDecimal investAmount;

    private BigDecimal feeRate;

    @Setter
    private boolean favorite;

    @Setter
    private LocalDateTime dateTime;

    @Setter
    private BigDecimal totalAmount;

    @Setter
    private BigDecimal profitAmount;

    @Setter
    private BigDecimal profitPercentage;

    @Builder.Default
    private Balance balance = new Balance();

    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @Builder.Default
    private SimulateReport simulateReport = new SimulateReport();

    public enum Status { WAITING, RUNNING, COMPLETED, STOPPING, STOPPED, FAILED }

    @Converter(autoApply = true)
    public static class StatusConverter extends AbstractEnumConverter<Status> {}

    public static Simulate from(SimulateEntity simulateEntity) {
        ObjectMapper objectMapper = ObjectMapperHolder.getObject();

        // trade data
        Trade trade = new Trade();
        if(simulateEntity.getTradeData() != null) {
            try {
                trade = objectMapper.readValue(simulateEntity.getTradeData(), Trade.class);
            } catch (JsonProcessingException e) {
                log.debug(e.getMessage());
            }
        }

        // strategy data
        Strategy strategy = new Strategy();
        if (simulateEntity.getStrategyData() != null) {
            try {
                strategy = objectMapper.readValue(simulateEntity.getStrategyData(), Strategy.class);
            } catch (JsonProcessingException e) {
                log.debug(e.getMessage());
            }
        }

        // balance data
        Balance balance = new Balance();
        if(simulateEntity.getBalanceData() != null) {
            try {
                balance = objectMapper.readValue(simulateEntity.getBalanceData(), Balance.class);
            } catch (JsonProcessingException e) {
                log.debug(e.getMessage());
            }
        }

        // orders data
        List<Order> orders = new ArrayList<>();
        if(simulateEntity.getOrdersData() != null) {
            try {
                orders = objectMapper.readValue(simulateEntity.getOrdersData(), new TypeReference<>(){});
            } catch (JsonProcessingException e) {
                log.debug(e.getMessage());
            }
        }

        // simulate report
        SimulateReport simulateReport = new SimulateReport();
        if(simulateEntity.getSimulateReportData() != null) {
            try {
                simulateReport = objectMapper.readValue(simulateEntity.getSimulateReportData(), new TypeReference<>(){});
            } catch (JsonProcessingException e) {
                log.debug(e.getMessage());
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
                .strategy(strategy)
                .dateTimeFrom(simulateEntity.getDateTimeFrom())
                .dateTimeTo(simulateEntity.getDateTimeTo())
                .investAmount(simulateEntity.getInvestAmount())
                .feeRate(simulateEntity.getFeeRate())
                .dateTime(simulateEntity.getDateTime())
                .favorite(simulateEntity.isFavorite())
                .totalAmount(simulateEntity.getTotalAmount())
                .profitAmount(simulateEntity.getProfitAmount())
                .profitPercentage(simulateEntity.getProfitPercentage())
                .balance(balance)
                .orders(orders)
                .simulateReport(simulateReport)
                .build();
    }

}
