package org.oopscraft.fintics.thread;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.oopscraft.arch4j.web.support.SseLogAppender;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.MarketService;
import org.oopscraft.fintics.service.TradeService;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TradeRunnable implements Runnable {

    @Getter
    private final String tradeId;

    @Getter
    private final Integer interval;

    @Getter
    private final SseLogAppender sseLogAppender;

    private final PlatformTransactionManager transactionManager;

    private final TradeService tradeService;

    private final MarketService marketService;

    private final Logger log;

    @Setter
    @Getter
    private boolean interrupted = false;

    @Builder
    public TradeRunnable(String tradeId, Integer interval, ApplicationContext applicationContext, SseLogAppender sseLogAppender) {
        this.tradeId = tradeId;
        this.interval = interval;
        this.sseLogAppender = sseLogAppender;

        // component
        this.transactionManager = applicationContext.getBean(PlatformTransactionManager.class);
        this.tradeService = applicationContext.getBean(TradeService.class);
        this.marketService = applicationContext.getBean(MarketService.class);

        // add log appender
        log = (Logger) LoggerFactory.getLogger(tradeId);
        log.addAppender(this.sseLogAppender);
    }

    @Override
    public void run() {
        this.sseLogAppender.start();
        log.info("Start TradeRunnable: {}", tradeId);
        while(!Thread.currentThread().isInterrupted() && !interrupted) {
            TransactionStatus transactionStatus = null;
            try {
                Thread.sleep(3_000);

                // start transaction
                DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
                transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                transactionStatus = transactionManager.getTransaction(transactionDefinition);

                // execute trade
                executeTrade();

                // wait interval
                log.info("Waiting interval: {} seconds", interval);
                Thread.sleep(interval * 1_000);

            } catch (InterruptedException e) {
                log.warn("TradeRunnable is interrupted.");
                break;
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            } finally {
                if(transactionStatus != null) {
                    transactionManager.commit(transactionStatus);
                }
            }
        }
        log.info("End TradeRunnable: {}", tradeId);
        this.sseLogAppender.stop();
    }

    private void executeTrade() throws InterruptedException {
        try {
            // load trade info
            Trade trade = tradeService.getTrade(tradeId).orElseThrow();

            // checks start,end time
            LocalDateTime dateTime = LocalDateTime.now();
            if (!isOperatingTime(trade, dateTime.toLocalTime())) {
                log.info("Not operating time - [{}] {} ~ {}", trade.getName(), trade.getStartAt(), trade.getEndAt());
                return;
            }

            // logging
            log.info("Check trade - [{}]", trade.getName());

            // balance
            Balance balance = tradeService.getTradeBalance(trade.getTradeId()).orElseThrow();

            // checks buy condition
            for (TradeAsset tradeAsset : trade.getTradeAssets()) {
                Thread.sleep(100);

                // check enabled
                if (!tradeAsset.isEnabled()) {
                    continue;
                }

                // logging
                log.info("Check asset - [{}]", tradeAsset.getName());

                // binding variables
                AssetIndicator assetIndicator = tradeService.getTradeAssetIndicator(trade.getTradeId(), tradeAsset.getSymbol())
                        .orElseThrow();
                Market market = marketService.getMarket();

                // executes trade asset decider
                TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                        .holdCondition(trade.getHoldCondition())
                        .logger(log)
                        .dateTime(dateTime)
                        .assetIndicator(assetIndicator)
                        .market(market)
                        .build();
                Boolean holdConditionResult = tradeAssetDecider.execute();
                log.info("holdConditionResult: {}", holdConditionResult);

                // 1. null is no operation
                if (holdConditionResult == null) {
                    continue;
                }

                // 2. buy and hold
                if (holdConditionResult.equals(Boolean.TRUE)) {
                    if (!balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                        BigDecimal buyAmount = balance.getTotalAmount()
                                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL128)
                                .multiply(tradeAsset.getHoldRatio())
                                .setScale(2, RoundingMode.HALF_UP);
                        BigDecimal askPrice = assetIndicator.getOrderBook().getAskPrice();
                        int quantity = buyAmount
                                .divide(askPrice, 0, RoundingMode.FLOOR)
                                .intValue();
                        try {
                            log.info("Buy asset: {}", tradeAsset.getName());
                            tradeService.buyTradeAsset(tradeAsset.getTradeId(), tradeAsset.getSymbol(), quantity);
                        } catch (Throwable e) {
                            log.warn(e.getMessage());
                            tradeService.sendErrorAlarmIfEnabled(tradeId, e);
                        }
                    }
                }

                // 3. sell
                else if (holdConditionResult.equals(Boolean.FALSE)) {
                    if (balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                        BalanceAsset balanceAsset = balance.getBalanceAsset(tradeAsset.getSymbol()).orElseThrow();
                        Integer orderableQuantity = balanceAsset.getOrderableQuantity();
                        try {
                            log.info("Sell asset: {}", tradeAsset.getName());
                            tradeService.sellBalanceAsset(tradeAsset.getTradeId(), tradeAsset.getSymbol(), orderableQuantity);
                        } catch (Throwable e) {
                            log.warn(e.getMessage());
                            tradeService.sendErrorAlarmIfEnabled(tradeId, e);
                        }
                    }
                }
            }
        } catch(InterruptedException e) {
            throw e;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            tradeService.sendErrorAlarmIfEnabled(tradeId, e);
        }
    }


    private boolean isOperatingTime(Trade trade, LocalTime time) {
        if(trade.getStartAt() == null || trade.getEndAt() == null) {
            return false;
        }
        return time.isAfter(trade.getStartAt()) && time.isBefore(trade.getEndAt());
    }

}
