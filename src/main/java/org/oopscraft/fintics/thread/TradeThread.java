package org.oopscraft.fintics.thread;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.client.Client;
import org.oopscraft.fintics.client.ClientFactory;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TradeThread extends Thread {

    @Getter
    private final Trade trade;

    private final AlarmService alarmService;

    @Getter
    private final Map<String, AssetIndicator> assetIndicatorMap = new ConcurrentHashMap<>();

    @Builder
    public TradeThread(Trade trade, AlarmService alarmService) {
        this.trade = trade;
        this.alarmService = alarmService;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            try {
                Thread.sleep(trade.getInterval() * 1_000);

                // checks start,end time
                if(!isOperatingTime(LocalTime.now())) {
                    log.info("== {}\t{} - not operating time. {} ~ {}", trade.getTradeId(), trade.getName(), trade.getStartAt(), trade.getEndAt());
                    continue;
                }
                log.info("== {}\t{} - checks trade interval.", trade.getTradeId(), trade.getName());

                // creates interface client
                Client client = ClientFactory.getClient(trade);
                Balance balance = client.getBalance();

                // checks buy condition
                for(TradeAsset tradeAsset : trade.getTradeAssets()) {

                    // check enabled
                    if(!tradeAsset.isEnabled()) {
                        continue;
                    }

                    // force delay
                    Thread.sleep(1000);

                    // build asset indicator
                    OrderBook orderBook = client.getOrderBook(tradeAsset);
                    List<Ohlcv> minuteOhlcvs = client.getMinuteOhlcvs(tradeAsset);
                    List<Ohlcv> dailyOhlcvs = client.getDailyOhlcvs(tradeAsset);
                    AssetIndicator assetIndicator = AssetIndicator.builder()
                            .asset(tradeAsset)
                            .orderBook(orderBook)
                            .minuteOhlcvs(minuteOhlcvs)
                            .dailyOhlcvs(dailyOhlcvs)
                            .build();
                    assetIndicatorMap.put(assetIndicator.getSymbol(), assetIndicator);

                    // decides hold condition
                    Boolean holdConditionResult = getHoldConditionResult(assetIndicator);
                    assetIndicator.setHoldConditionResult(holdConditionResult);

                    // 1. null is no operation
                    if(holdConditionResult == null) {
                        continue;
                    }

                    // 2. buy and hold
                    if(holdConditionResult.equals(Boolean.TRUE)) {
                        if(!balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                            BigDecimal buyAmount = BigDecimal.valueOf(balance.getTotalAmount())
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(tradeAsset.getHoldRatio()));
                            Double askPrice = assetIndicator.getOrderBook().getAskPrice();
                            int quantity = buyAmount
                                    .divide(BigDecimal.valueOf(askPrice), 0, RoundingMode.FLOOR)
                                    .intValue();
                            try {
                                client.buyAsset(tradeAsset, quantity);
                                sendBuyOrderAlarmIfEnabled(tradeAsset, quantity);
                            }catch(Throwable e) {
                                log.warn(e.getMessage());
                                sendErrorAlarmIfEnabled(e);
                            }
                        }
                    }

                    // 3. sell
                    else if(holdConditionResult.equals(Boolean.FALSE)) {
                        if(balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                            BalanceAsset balanceAsset = balance.getBalanceAsset(tradeAsset.getSymbol());
                            Integer quantity = balanceAsset.getQuantity();
                            try {
                                client.sellAsset(balanceAsset, quantity);
                                sendSellOrderAlarmIfEnabled(balanceAsset, quantity);
                            }catch(Throwable e) {
                                log.warn(e.getMessage());
                                sendErrorAlarmIfEnabled(e);
                            }
                        }
                    }
                }

            } catch (InterruptedException e) {
                log.warn(e.getMessage());
                break;
            } catch (Throwable t) {
                log.warn(t.getMessage());
                sendErrorAlarmIfEnabled(t);
            }
        }
    }

    private Boolean getHoldConditionResult(AssetIndicator assetIndicator) {
        Binding binding = new Binding();
        binding.setVariable("assetIndicator", assetIndicator);
        GroovyShell groovyShell = new GroovyShell(binding);
        String script = trade.getHoldCondition();
        if(script == null || script.isBlank()) {
            return null;
        }
        Object result = groovyShell.evaluate(script);
        if(result == null) {
            return null;
        }
        return (Boolean) result;
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

    private void sendErrorAlarmIfEnabled(Throwable t) {
        if(trade.isAlarmOnError()) {
            sendAlarmIfEnabled(t.getMessage(), ExceptionUtils.getStackTrace(t));
        }
    }

    private void sendOrderAlarmIfEnabled(String subject, String content) {
        if(trade.isAlarmOnOrder()) {
            sendAlarmIfEnabled(subject, content);
        }
    }

    private void sendBuyOrderAlarmIfEnabled(TradeAsset tradeAsset, int quantity) {
        String subject = String.format("Buy [%s], %d", tradeAsset.getName(), quantity);
        sendOrderAlarmIfEnabled(subject, subject);
    }

    private void sendSellOrderAlarmIfEnabled(BalanceAsset balanceAsset, int quantity) {
        String subject = String.format("Sell [%s], %d", balanceAsset.getName(), quantity);
        sendOrderAlarmIfEnabled(subject, subject);
    }

}
