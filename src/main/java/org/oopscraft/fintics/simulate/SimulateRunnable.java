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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class SimulateRunnable implements Runnable {

    @Getter
    private final Simulate simulate;

    private final SimulateIndiceClient simulateIndiceClient;

    private final SimulateTradeClient simulateTradeClient;

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
            SimulateTradeClient simulateTradeClient,
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
        saveSimulate(simulate);

        if(this.simulateLogAppender != null) {
            log.addAppender(this.simulateLogAppender);
            this.simulateLogAppender.start();
        }
        try {
            Trade trade = simulate.getTrade();
            LocalDateTime dateTimeFrom = simulate.getDateTimeFrom();
            LocalDateTime dateTimeTo = simulate.getDateTimeTo();
            Integer interval = trade.getInterval();

            // invest amount, fee rate
            BigDecimal investAmount = simulate.getInvestAmount();
            BigDecimal feeRate = simulate.getFeeRate();
            simulateTradeClient.deposit(investAmount);
            simulateTradeClient.setFeeRate(feeRate);

            // add order listener
            simulateTradeClient.onOrder(order -> {
                sendMessage("order", order);
            });

            // trade executor
            TradeExecutor tradeExecutor = tradeExecutorFactory.getObject();
            tradeExecutor.setLog(log);

            // start
            for(LocalDateTime dateTime = dateTimeFrom.plusSeconds(interval); dateTime.isBefore(dateTimeTo); dateTime = dateTime.plusSeconds(interval)) {
                // check interrupted
                if(interrupted) {
                    log.info("SimulateRunnable is interrupted");
                    break;
                }
                // check start and end time
                if(dateTime.toLocalTime().isBefore(trade.getStartAt()) || dateTime.toLocalTime().isAfter(trade.getEndAt())) {
                    continue;
                }

                log.info("== dateTime:{}", dateTime);
                TransactionStatus transactionStatus = null;
                try {
                    simulateIndiceClient.setDateTime(dateTime);
                    simulateTradeClient.setDateTime(dateTime);

                    // check market open
                    if(!simulateTradeClient.isOpened(dateTime)) {
                        log.info("market not open:{}", dateTime);
                        continue;
                    }

                    // start transaction
                    DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
                    transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    transactionStatus = transactionManager.getTransaction(transactionDefinition);

                    // executes trade
                    tradeExecutor.execute(trade, dateTime, simulateIndiceClient, simulateTradeClient);

                    // send message
                    HashMap<String,String> status = new LinkedHashMap<>();
                    status.put("dateTime", dateTime.format(DateTimeFormatter.ISO_DATE_TIME));
                    sendMessage("status", status);
                    sendMessage("balance", simulateTradeClient.getBalance());

                } catch (Throwable e) {
                    log.warn(e.getMessage(), e);
                } finally {
                    if(transactionStatus != null) {
                        if(!transactionStatus.isCompleted()) {
                            transactionStatus.setRollbackOnly();
                            transactionManager.commit(transactionStatus);
                        }
                    }
                }
            }

            // save history
            simulate.setStatus(Simulate.Status.COMPLETED);

        }catch(Exception e) {
            log.error(e.getMessage(), e);
            simulate.setStatus(Simulate.Status.FAILED);
            throw new RuntimeException(e);
        }finally{
            simulateLogAppender.stop();
            this.onComplete.run();

            // save history
            simulate.setEndedAt(LocalDateTime.now());
            saveSimulate(simulate);
        }
    }

    private void sendMessage(String destinationSuffix, Object object) {
        String destination =  String.format("/simulates/%s/%s", simulate.getSimulateId(), destinationSuffix);
        String message = null;
        try {
            message = objectMapper.writeValueAsString(object);
            messagingTemplate.convertAndSend(destination, message);
        }catch( JsonProcessingException e) {
            log.error("== object:{}", object);
            log.error(e.getMessage(), e);
        }
    }

    public void onComplete(Runnable listener) {
        this.onComplete = listener;
    }

    public void saveSimulate(Simulate simulate) {
        SimulateEntity simulateEntity = simulateRepository.findById(simulate.getSimulateId())
                .orElse(null);
        if(simulateEntity == null) {
            simulateEntity = SimulateEntity.builder()
                    .simulateId(simulate.getSimulateId())
                    .tradeId(simulate.getTradeId())
                    .tradeName(simulate.getTradeName())
                    .tradeData(toDataString(simulate.getTrade()))
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
        simulateEntity.setBalanceData(toDataString(simulate.getBalance()));
        simulateEntity.setOrdersData(toDataString(simulate.getOrders()));

        simulateRepository.saveAndFlush(simulateEntity);
    }

    private String toDataString(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException ignored) {
            return null;
        }
    }

}
