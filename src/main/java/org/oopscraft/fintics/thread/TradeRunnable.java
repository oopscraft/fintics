package org.oopscraft.fintics.thread;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.arch4j.web.support.SseLogAppender;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.TradeService;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TradeRunnable implements Runnable {

    @Getter
    private final Trade trade;

    @Getter
    private final SseLogAppender sseLogAppender;

    private final PlatformTransactionManager transactionManager;

    private final TradeService tradeService;

    private final AlarmService alarmService;

    private final Logger log;

    @Builder
    public TradeRunnable(ApplicationContext applicationContext, Trade trade, SseLogAppender sseLogAppender) {
        this.trade = trade;
        this.sseLogAppender = sseLogAppender;

        // component
        this.transactionManager = applicationContext.getBean(PlatformTransactionManager.class);
        this.tradeService = applicationContext.getBean(TradeService.class);
        this.alarmService = applicationContext.getBean(AlarmService.class);

        // add log appender
        log = (Logger) LoggerFactory.getLogger(trade.getTradeId());
        ((Logger)log).addAppender(this.sseLogAppender);
    }

    @Override
    public void run() {
        log.info("Start Trade Thread: {}", trade);
        while(!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(trade.getInterval() * 1_000);
                LocalDateTime dateTime = LocalDateTime.now();

                DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
                transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager, transactionDefinition);
                transactionTemplate.executeWithoutResult(transactionStatus -> {

                    // checks start,end time
                    if (!isOperatingTime(dateTime.toLocalTime())) {
                        log.info("Not operating time - [{}] {} ~ {}", trade.getName(), trade.getStartAt(), trade.getEndAt());
                        return;
                    }

                    // logging
                    log.info("Check trade - [{}]", trade.getName());

                    // balance
                    Balance balance = tradeService.getTradeBalance(trade.getTradeId()).orElseThrow();

                    // checks buy condition
                    for (TradeAsset tradeAsset : trade.getTradeAssets()) {

                        // check enabled
                        if (!tradeAsset.isEnabled()) {
                            continue;
                        }

                        // logging
                        log.info("Check asset - [{}]", tradeAsset.getName());

                        // build asset indicator
                        AssetIndicator assetIndicator = tradeService.getTradeAssetIndicator(trade.getTradeId(), tradeAsset.getSymbol())
                                .orElseThrow();

                        // decides hold condition
                        TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                                .trade(trade)
                                .tradeAsset(tradeAsset)
                                .assetIndicator(assetIndicator)
                                .dateTime(dateTime)
                                .logger(log)
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
                                    sendErrorAlarmIfEnabled(trade, e);
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
                                    sendErrorAlarmIfEnabled(trade, e);
                                }
                            }
                        }
                    }

                });

            } catch(InterruptedException e) {
                log.warn(e.getMessage());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                sendErrorAlarmIfEnabled(trade, e);
            }
        }
    }

    private boolean isOperatingTime(LocalTime time) {
        if(trade.getStartAt() == null || trade.getEndAt() == null) {
            return false;
        }
        return time.isAfter(trade.getStartAt()) && time.isBefore(trade.getEndAt());
    }

    private void sendErrorAlarmIfEnabled(Trade trade, Throwable t) {
        if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
            if (trade.isAlarmOnError()) {
                String subject = String.format("[%s]", trade.getName());
                alarmService.sendAlarm(trade.getAlarmId(), subject, ExceptionUtils.getStackTrace(t));
            }
        }
    }

}
