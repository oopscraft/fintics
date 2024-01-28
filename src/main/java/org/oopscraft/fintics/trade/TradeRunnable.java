package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.Trade;
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

    private final TradeRepository tradeRepository;

    private final TradeExecutorFactory tradeExecutorFactory;

    private final IndiceClient indiceClient;

    private final BrokerClientFactory brokerClientFactory;

    private final PlatformTransactionManager transactionManager;

    private final Logger log;

    @Setter
    private TradeLogAppender tradeLogAppender;

    @Setter
    @Getter
    private boolean interrupted = false;

    @Builder
    protected TradeRunnable(
        String tradeId,
        Integer interval,
        TradeRepository tradeRepository,
        TradeExecutorFactory tradeExecutorFactory,
        IndiceClient indiceClient,
        BrokerClientFactory brokerClientFactory,
        PlatformTransactionManager transactionManager
    ){
        this.tradeId = tradeId;
        this.interval = interval;
        this.tradeRepository = tradeRepository;
        this.tradeExecutorFactory = tradeExecutorFactory;
        this.indiceClient = indiceClient;
        this.brokerClientFactory = brokerClientFactory;
        this.transactionManager = transactionManager;

        // log
        this.log = (Logger) LoggerFactory.getLogger(tradeId);
    }

    @Override
    public void run() {
        if(this.tradeLogAppender != null) {
            log.addAppender(this.tradeLogAppender);
            this.tradeLogAppender.start();
        }
        log.info("Start TradeRunnable: {}", tradeId);

        // start loop
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
                TradeExecutor tradeExecutor = tradeExecutorFactory.getObject();
                tradeExecutor.setLog(log);
                LocalDateTime dateTime = LocalDateTime.now();
                Trade trade = tradeRepository.findById(tradeId)
                        .map(Trade::from)
                        .orElseThrow();
                BrokerClient brokerClient = brokerClientFactory.getObject(trade);
                tradeExecutor.execute(trade, dateTime, indiceClient, brokerClient);

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
        if(this.tradeLogAppender != null) {
            this.tradeLogAppender.stop();
            log.detachAppender(this.tradeLogAppender);
        }
    }

}
