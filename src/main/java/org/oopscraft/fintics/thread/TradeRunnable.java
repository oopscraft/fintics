package org.oopscraft.fintics.thread;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.arch4j.web.support.SseLogAppender;
import org.oopscraft.fintics.client.Client;
import org.oopscraft.fintics.client.ClientFactory;
import org.oopscraft.fintics.model.*;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class TradeRunnable implements Runnable {

    @Getter
    private final Trade trade;

    private final Logger log;

    @Getter
    private final SseLogAppender sseLogAppender;

    private final AlarmService alarmService;

    private final Client client;

    @Builder
    public TradeRunnable(Trade trade, SseLogAppender sseLogAppender, AlarmService alarmService) {
        this.trade = trade;
        this.sseLogAppender = sseLogAppender;
        this.alarmService = alarmService;

        // add log appender
        log = (Logger) LoggerFactory.getLogger(trade.getTradeId());
        ((Logger)log).addAppender(this.sseLogAppender);

        // creates client
        this.client = ClientFactory.getClient(trade.getClientType(), trade.getClientProperties());
    }

    @Override
    public void run() {
        log.info("Start Trade Thread: {}", trade);
        while(!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(trade.getInterval() * 1_000);
                LocalDateTime dateTime = LocalDateTime.now();

                // checks start,end time
                if (!isOperatingTime(dateTime.toLocalTime())) {
                    log.info("Not operating time - [{}] {} ~ {}", trade.getName(), trade.getStartAt(), trade.getEndAt());
                    continue;
                }

                // logging
                log.info("Check trade - [{}]", trade.getName());

                // balance
                Balance balance = client.getBalance();

                // checks buy condition
                for (TradeAsset tradeAsset : trade.getTradeAssets()) {

                    // check enabled
                    if (!tradeAsset.isEnabled()) {
                        continue;
                    }

                    // force delay
                    Thread.sleep(1000);

                    // logging
                    log.info("Check asset - [{}]", tradeAsset.getName());

                    // build asset indicator
                    OrderBook orderBook = client.getOrderBook(tradeAsset);
                    List<Ohlcv> minuteOhlcvs = client.getMinuteOhlcvs(tradeAsset);
                    log.info("Latest minuteOhlcv.dateTime: {}", minuteOhlcvs.size() < 1 ? null : minuteOhlcvs.get(0).getDateTime());
                    List<Ohlcv> dailyOhlcvs = client.getDailyOhlcvs(tradeAsset);
                    log.info("Latest dailyOhlcvs.dateTime: {}", dailyOhlcvs.size() < 1 ? null : dailyOhlcvs.get(0).getDateTime());
                    AssetIndicator assetIndicator = AssetIndicator.builder()
                            .asset(tradeAsset)
                            .orderBook(orderBook)
                            .minuteOhlcvs(minuteOhlcvs)
                            .dailyOhlcvs(dailyOhlcvs)
                            .build();

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
                                client.buyAsset(tradeAsset, quantity);
                                sendBuyOrderAlarmIfEnabled(trade, tradeAsset, quantity);
                            } catch (Throwable e) {
                                log.warn(e.getMessage());
                                sendErrorAlarmIfEnabled(trade, e);
                            }
                        }
                    }

                    // 3. sell
                    else if (holdConditionResult.equals(Boolean.FALSE)) {
                        if (balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                            BalanceAsset balanceAsset = balance.getBalanceAsset(tradeAsset.getSymbol());
                            Integer orderableQuantity = balanceAsset.getOrderableQuantity();
                            try {
                                log.info("Sell asset: {}", tradeAsset.getName());
                                client.sellAsset(balanceAsset, orderableQuantity);
                                sendSellOrderAlarmIfEnabled(trade, balanceAsset, orderableQuantity);
                            } catch (Throwable e) {
                                log.warn(e.getMessage());
                                sendErrorAlarmIfEnabled(trade, e);
                            }
                        }
                    }
                }

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

    private void sendAlarmIfEnabled(String subject, String content) {
        if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
            alarmService.sendAlarm(trade.getAlarmId(), subject, content);
        }
    }

    private void sendErrorAlarmIfEnabled(Trade trade, Throwable t) {
        if(trade.isAlarmOnError()) {
            String subject = String.format("[%s]", trade.getName());
            sendAlarmIfEnabled(subject, ExceptionUtils.getStackTrace(t));
        }
    }

    private void sendOrderAlarmIfEnabled(Trade trade, String content) {
        if(trade.isAlarmOnOrder()) {
            String subject = String.format("[%s]", trade.getName());
            sendAlarmIfEnabled(subject, content);
        }
    }

    private void sendBuyOrderAlarmIfEnabled(Trade trade, TradeAsset tradeAsset, int quantity) {
        String content = String.format("[%s] Buy %d", tradeAsset.getName(), quantity);
        sendOrderAlarmIfEnabled(trade, content);
    }

    private void sendSellOrderAlarmIfEnabled(Trade trade, BalanceAsset balanceAsset, int quantity) {
        String content = String.format("[%s] Sell %d", balanceAsset.getName(), quantity);
        sendOrderAlarmIfEnabled(trade, content);
    }

}
