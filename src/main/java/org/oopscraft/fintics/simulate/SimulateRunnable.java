package org.oopscraft.fintics.simulate;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.oopscraft.fintics.dao.SimulateEntity;
import org.oopscraft.fintics.dao.SimulateRepository;
import org.oopscraft.fintics.model.Simulate;
import org.oopscraft.fintics.model.Strategy;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.trade.*;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SimulateRunnable implements Runnable {

    @Getter
    private final Simulate simulate;

    private final SimulateBrokerClient simulateBrokerClient;

    private final TradeExecutorFactory tradeExecutorFactory;

    private final SimulateRepository simulateRepository;

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    private final TradeAssetStoreFactory statusHandlerFactory;

    @Setter
    private Logger log;

    @Setter
    private LogAppender logAppender;

    @Setter
    @Getter
    private boolean interrupted = false;

    private Runnable onComplete;

    @Builder
    protected SimulateRunnable(
            Simulate simulate,
            SimulateBrokerClient simulateTradeClient,
            TradeExecutorFactory tradeExecutorFactory,
            SimulateRepository simulateRepository,
            SimpMessagingTemplate messagingTemplate,
            ObjectMapper objectMapper,
            TradeAssetStoreFactory statusHandlerFactory
    ){
        this.simulate = simulate;
        this.simulateBrokerClient = simulateTradeClient;
        this.tradeExecutorFactory = tradeExecutorFactory;
        this.simulateRepository = simulateRepository;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.statusHandlerFactory = statusHandlerFactory;

        // log
        this.log = (Logger) LoggerFactory.getLogger(simulate.getSimulateId());
    }

    @Override
    public void run() {
        simulate.setStatus(Simulate.Status.RUNNING);
        simulate.setStartedAt(Instant.now());

        if (this.logAppender != null) {
            log.addAppender(logAppender);
            logAppender.start();
        }
        try {
            Trade trade = simulate.getTrade();
            Strategy strategy = simulate.getStrategy();

            // disable alarm
            trade.setAlarmId(null);
            trade.setAlarmOnError(false);
            trade.setAlarmOnOrder(false);

            // date time, interval
            LocalDateTime investFrom = simulate.getInvestFrom();
            LocalDateTime investTo = simulate.getInvestTo();
            Integer interval = trade.getInterval();

            // invest amount, fee rate
            BigDecimal investAmount = simulate.getInvestAmount();
            trade.setInvestAmount(investAmount);

            // deposit
            simulateBrokerClient.setDatetime(investFrom);
            simulateBrokerClient.deposit(investAmount);

            // start
            saveSimulate();

            // trade executor
            TradeExecutor tradeExecutor = tradeExecutorFactory.getObject();
            tradeExecutor.setLog(log);

            // status handler
            String destination = String.format("/simulates/%s/message", simulate.getSimulateId());
            TradeAssetStore statusHandler = statusHandlerFactory.getObject(destination, false);
            tradeExecutor.setTradeAssetStore(statusHandler);

            // loop
            for (LocalDateTime dateTime = investFrom;
                 dateTime.isBefore(investTo) || dateTime.equals(investTo);
                 dateTime = dateTime.plusSeconds(interval)
            ) {
                try {
                    // change date time
                    log.info("== datetime:{}", dateTime);
                    simulate.setDatetime(dateTime);
                    simulateBrokerClient.setDatetime(dateTime);
                    sendMessage("dateTime", DateTimeFormatter.ISO_DATE_TIME.format(dateTime));

                    // check interrupted
                    if (interrupted) {
                        throw new InterruptedException("SimulateRunnable is interrupted.");
                    }
                    // check start and end time
                    if (!isOperatingTime(trade, dateTime)) {
                        continue;
                    }

                    // check market open
                    if (!simulateBrokerClient.isOpened(dateTime)) {
                        log.info("market not open:{}", dateTime);
                        continue;
                    }

                    // executes trade
                    tradeExecutor.execute(trade, strategy, dateTime, simulateBrokerClient);

                    // send balance message
                    sendMessage("balance", simulateBrokerClient.getBalance());
                    sendMessage("orders", simulateBrokerClient.getOrders());
                    sendMessage("simulateReport", simulateBrokerClient.getSimulateReport());

                } catch (InterruptedException e) {
                    log.warn(e.getMessage(), e);
                    throw e;
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                } finally {
                    saveSimulate();
                }
            }

            // save history
            simulate.setStatus(Simulate.Status.COMPLETED);

        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
            simulate.setStatus(Simulate.Status.STOPPED);
        } catch (Exception e) {
            simulate.setStatus(Simulate.Status.FAILED);
        } finally {
            if (logAppender != null) {
                logAppender.stop();
            }
            this.onComplete.run();

            // save history
            simulate.setEndedAt(Instant.now());
            saveSimulate();
        }
    }

    private boolean isOperatingTime(Trade trade, LocalDateTime datetime) {
        if(trade.getStartTime() == null || trade.getEndTime() == null) {
            return false;
        }
        LocalTime startTime = trade.getStartTime();
        LocalTime endTime = trade.getEndTime();
        LocalTime currentTime = datetime.toLocalTime();
        if (startTime.isAfter(endTime)) {
            return !(currentTime.isBefore(startTime) || currentTime.equals(startTime))
                    || !(currentTime.isAfter(endTime) || currentTime.equals(endTime));
        } else {
            return (currentTime.isAfter(startTime) || currentTime.equals(startTime))
                    && (currentTime.isBefore(endTime) || currentTime.equals(endTime));
        }
    }

    private void sendMessage(String destinationSuffix, Object object) {
        String destination =  String.format("/simulates/%s/%s", simulate.getSimulateId(), destinationSuffix);
        String message;
        if(object != null) {
            try {
                message = objectMapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                message = e.getMessage();
            }
            messagingTemplate.convertAndSend(destination, message);
        }
    }

    public void onComplete(Runnable listener) {
        this.onComplete = listener;
    }

    public void saveSimulate() {
        // find previous simulate entity
        SimulateEntity simulateEntity = simulateRepository.findById(simulate.getSimulateId())
                .orElse(null);
        if(simulateEntity == null) {
            simulateEntity = SimulateEntity.builder()
                    .simulateId(simulate.getSimulateId())
                    .tradeId(simulate.getTradeId())
                    .tradeName(simulate.getTradeName())
                    .tradeData(toDataString(simulate.getTrade()))
                    .strategyData(toDataString(simulate.getStrategy()))
                    .investFrom(simulate.getInvestFrom())
                    .investTo(simulate.getInvestTo())
                    .investAmount(simulate.getInvestAmount())
                    .feeRate(simulate.getFeeRate())
                    .startedAt(Instant.now())
                    .build();
        }
        simulateEntity.setStartedAt(simulate.getStartedAt());
        simulateEntity.setEndedAt(simulate.getEndedAt());
        simulateEntity.setStatus(simulate.getStatus());
        simulateEntity.setDateTime(simulate.getDatetime());

        // setting detail properties
        try {
            BigDecimal investAmount = simulateEntity.getInvestAmount();
            BigDecimal balanceTotalAmount = simulateBrokerClient.getBalance().getTotalAmount();
            BigDecimal profitAmount = balanceTotalAmount.subtract(investAmount);
            BigDecimal profitPercentage = profitAmount.divide(investAmount, MathContext.DECIMAL32)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.FLOOR);
            simulateEntity.setProfitAmount(profitAmount);
            simulateEntity.setProfitPercentage(profitPercentage);
            simulateEntity.setBalanceData(toDataString(simulateBrokerClient.getBalance()));
            simulateEntity.setOrdersData(toDataString(simulateBrokerClient.getOrders()));
            simulateEntity.setSimulateReportData(toDataString(simulateBrokerClient.getSimulateReport()));
        } catch (Exception ignore) {
            log.warn(ignore.getMessage());
        }

        // save
        simulateRepository.saveAndFlush(simulateEntity);
    }

    private String toDataString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

}
