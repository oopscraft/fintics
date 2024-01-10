package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.Trade;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;

public class TradeRunnable implements Runnable {

    @Getter
    private final String id;

    @Getter
    private final Integer interval;

    @Getter
    private final ApplicationContext applicationContext;

    @Getter
    private final TradeLogAppender tradeLogAppender;

    private final PlatformTransactionManager transactionManager;

    private final TradeRepository tradeRepository;

    private final Logger log;

    private final TradeExecutor tradeExecutor;

    @Setter
    @Getter
    private boolean interrupted = false;

    @Builder
    protected TradeRunnable(String id, Integer interval, ApplicationContext applicationContext, TradeLogAppender tradeLogAppender) {
        this.id = id;
        this.interval = interval;
        this.applicationContext = applicationContext;
        this.tradeLogAppender = tradeLogAppender;

        // component
        this.transactionManager = applicationContext.getBean(PlatformTransactionManager.class);
        this.tradeRepository = applicationContext.getBean(TradeRepository.class);

        // add log appender
        log = (Logger) LoggerFactory.getLogger(id);
        log.addAppender(this.tradeLogAppender);

        // trade executor
        this.tradeExecutor = TradeExecutor.builder()
                .applicationContext(applicationContext)
                .log(log)
                .build();
    }

    @Override
    public void run() {
        this.tradeLogAppender.start();
        log.info("Start TradeRunnable: {}", id);
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
                Trade trade = tradeRepository.findById(id)
                        .map(Trade::from)
                        .orElseThrow();
                tradeExecutor.execute(trade, dateTime);

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
        log.info("End TradeRunnable: {}", id);
        this.tradeLogAppender.stop();
    }

}
