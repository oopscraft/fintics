package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.model.Broker;
import org.oopscraft.fintics.model.Strategy;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.service.BrokerService;
import org.oopscraft.fintics.service.StrategyService;
import org.oopscraft.fintics.service.TradeService;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;

public class TradeRunnable implements Runnable {

    @Getter
    private final String tradeId;

    @Getter
    private final Integer interval;

    private final TradeService tradeService;

    private final StrategyService strategyService;

    private final BrokerService brokerService;

    private final TradeExecutor tradeExecutor;

    private final IndiceClient indiceClient;

    private final BrokerClientFactory brokerClientFactory;

    private final PlatformTransactionManager transactionManager;

    private final MessageTemplateFactory messageTemplateFactory;

    private final Logger log;

    @Setter
    private LogAppender logAppender;

    @Setter
    @Getter
    private boolean interrupted = false;

    @Builder
    protected TradeRunnable(
        String tradeId,
        Integer interval,
        TradeService tradeService,
        StrategyService strategyService,
        BrokerService brokerService,
        TradeExecutor tradeExecutor,
        IndiceClient indiceClient,
        BrokerClientFactory brokerClientFactory,
        MessageTemplateFactory messageTemplateFactory,
        PlatformTransactionManager transactionManager
    ){
        this.tradeId = tradeId;
        this.interval = interval;
        this.tradeService = tradeService;
        this.strategyService = strategyService;
        this.brokerService = brokerService;
        this.tradeExecutor = tradeExecutor;
        this.indiceClient = indiceClient;
        this.brokerClientFactory = brokerClientFactory;
        this.messageTemplateFactory = messageTemplateFactory;
        this.transactionManager = transactionManager;

        // log
        this.log = (Logger) LoggerFactory.getLogger(tradeId);
    }

    @Override
    public void run() {
        // logger
        tradeExecutor.setLog(log);
        if (this.logAppender != null) {
            log.addAppender(this.logAppender);
            this.logAppender.start();
        }

        // message template
        String destination = String.format("/trades/%s/message", tradeId);
        MessageTemplate messageTemplate = messageTemplateFactory.getObject(destination, message -> {
            tradeService.saveTradeAssetMessage(message.getTradeId(), message.getAssetId(), message.getBody());
        });
        tradeExecutor.setMessageTemplate(messageTemplate);

        // start loop
        log.info("Start TradeRunnable: {}", tradeId);
        while(!Thread.currentThread().isInterrupted() && !interrupted) {
            TransactionStatus transactionStatus = null;
            try {
                // wait interval
                log.info("Waiting interval: {} seconds", interval);
                Thread.sleep(interval * 1_000);

                // start transaction
                DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
                transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                transactionStatus = transactionManager.getTransaction(transactionDefinition);

                // call trade executor
                LocalDateTime dateTime = LocalDateTime.now();
                Trade trade = tradeService.getTrade(tradeId).orElseThrow();
                Strategy strategy = strategyService.getStrategy(trade.getStrategyId()).orElseThrow();
                Broker broker = brokerService.getBroker(trade.getBrokerId()).orElseThrow();
                BrokerClient brokerClient = brokerClientFactory.getObject(broker);
                tradeExecutor.execute(trade, strategy, dateTime, indiceClient, brokerClient);

                // end transaction
                transactionManager.commit(transactionStatus);

            } catch (InterruptedException e) {
                log.warn("TradeRunnable is interrupted.");
                break;
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            } finally {
                if(transactionStatus != null) {
                    if(!transactionStatus.isCompleted()) {
                        transactionStatus.setRollbackOnly();
                        transactionManager.commit(transactionStatus);
                    }
                }
            }
        }
        log.info("End TradeRunnable: {}", tradeId);
        if (this.logAppender != null) {
            this.logAppender.stop();
            log.detachAppender(this.logAppender);
        }
    }

}
