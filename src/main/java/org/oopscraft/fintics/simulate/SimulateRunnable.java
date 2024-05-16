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
import org.oopscraft.fintics.trade.TradeExecutor;
import org.oopscraft.fintics.trade.TradeExecutorFactory;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SimulateRunnable implements Runnable {

    @Getter
    private final Simulate simulate;

    private final SimulateIndiceClient simulateIndiceClient;

    private final SimulateBrokerClient simulateTradeClient;

    private final TradeExecutorFactory tradeExecutorFactory;

    private final PlatformTransactionManager transactionManager;

    private final SimulateRepository simulateRepository;

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    @Setter
    private Logger log;

    @Setter
    private SimulateLogAppender simulateLogAppender;

    @Setter
    @Getter
    private boolean interrupted = false;

    private Runnable onComplete;

    @Builder
    protected SimulateRunnable(
            Simulate simulate,
            SimulateIndiceClient simulateIndiceClient,
            SimulateBrokerClient simulateTradeClient,
            TradeExecutorFactory tradeExecutorFactory,
            PlatformTransactionManager transactionManager,
            SimulateRepository simulateRepository,
            SimpMessagingTemplate messagingTemplate,
            ObjectMapper objectMapper
    ){
        this.simulate = simulate;
        this.simulateIndiceClient = simulateIndiceClient;
        this.simulateTradeClient = simulateTradeClient;
        this.tradeExecutorFactory = tradeExecutorFactory;
        this.transactionManager = transactionManager;
        this.simulateRepository = simulateRepository;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;

        // log
        this.log = (Logger) LoggerFactory.getLogger(simulate.getSimulateId());
    }

    @Override
    public void run() {
        simulate.setStatus(Simulate.Status.RUNNING);
        simulate.setStartedAt(LocalDateTime.now());

        if(this.simulateLogAppender != null) {
            log.addAppender(this.simulateLogAppender);
            this.simulateLogAppender.start();
        }
        try {
            Trade trade = simulate.getTrade();
            Strategy strategy = simulate.getStrategy();

            // disable alarm
            trade.setAlarmId(null);
            trade.setAlarmOnError(false);
            trade.setAlarmOnOrder(false);

            // date time, interval
            LocalDateTime dateTimeFrom = simulate.getDateTimeFrom();
            LocalDateTime dateTimeTo = simulate.getDateTimeTo();
            Integer interval = trade.getInterval();

            // invest amount, fee rate
            BigDecimal investAmount = simulate.getInvestAmount();

            // deposit
            simulateTradeClient.setDateTime(dateTimeFrom);
            simulateTradeClient.deposit(investAmount);

            // start
            saveSimulate();

            // trade executor
            TradeExecutor tradeExecutor = tradeExecutorFactory.getObject();
            tradeExecutor.setLog(log);

            // loop
            for (LocalDateTime dateTime = dateTimeFrom;
                 dateTime.isBefore(dateTimeTo) || dateTime.isEqual(dateTimeTo);
                 dateTime = dateTime.plusSeconds(interval)
            ) {
                try {
                    // change date time
                    log.info("== dateTime:{}", dateTime);
                    simulate.setDateTime(dateTime);
                    simulateIndiceClient.setDateTime(dateTime);
                    simulateTradeClient.setDateTime(dateTime);
                    sendMessage("dateTime", dateTime.format(DateTimeFormatter.ISO_DATE_TIME));

                    // check interrupted
                    if (interrupted) {
                        throw new InterruptedException("SimulateRunnable is interrupted.");
                    }
                    // check start and end time
                    if (!isOperatingTime(trade, dateTime)) {
                        continue;
                    }

                    // check market open
                    if (!simulateTradeClient.isOpened(dateTime)) {
                        log.info("market not open:{}", dateTime);
                        continue;
                    }

                    // executes trade
                    tradeExecutor.execute(trade, strategy, dateTime, simulateIndiceClient, simulateTradeClient);

                    // send balance message
                    sendMessage("balance", simulateTradeClient.getBalance());
                    sendMessage("orders", simulateTradeClient.getOrders());
                    sendMessage("simulateReport", simulateTradeClient.getSimulateReport());

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
            simulateLogAppender.stop();
            this.onComplete.run();

            // save history
            simulate.setEndedAt(LocalDateTime.now());
            saveSimulate();
        }
    }

    private boolean isOperatingTime(Trade trade, LocalDateTime dateTime) {
        if(trade.getStartAt() == null || trade.getEndAt() == null) {
            return false;
        }
        LocalTime startTime = trade.getStartAt();
        LocalTime endTime = trade.getEndAt();
        LocalTime currentTime = dateTime.toLocalTime();
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
                    .dateTimeFrom(simulate.getDateTimeFrom())
                    .dateTimeTo(simulate.getDateTimeTo())
                    .investAmount(simulate.getInvestAmount())
                    .feeRate(simulate.getFeeRate())
                    .startedAt(LocalDateTime.now())
                    .build();
        }
        simulateEntity.setStartedAt(simulate.getStartedAt());
        simulateEntity.setEndedAt(simulate.getEndedAt());
        simulateEntity.setStatus(simulate.getStatus());
        simulateEntity.setDateTime(simulate.getDateTime());

        // setting detail properties
        try {
            BigDecimal investAmount = simulateEntity.getInvestAmount();
            BigDecimal balanceTotalAmount = simulateTradeClient.getBalance().getTotalAmount();
            BigDecimal profitAmount = balanceTotalAmount.subtract(investAmount);
            BigDecimal profitPercentage = profitAmount.divide(investAmount, MathContext.DECIMAL32)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.FLOOR);
            simulateEntity.setProfitAmount(profitAmount);
            simulateEntity.setProfitPercentage(profitPercentage);
            simulateEntity.setBalanceData(toDataString(simulateTradeClient.getBalance()));
            simulateEntity.setOrdersData(toDataString(simulateTradeClient.getOrders()));
            simulateEntity.setSimulateReportData(toDataString(simulateTradeClient.getSimulateReport()));
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
