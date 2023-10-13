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
                if(!isBetweenStartAndEndTime(LocalTime.now())) {
                    continue;
                }

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

                    // get indicator and decide hold
                    AssetIndicator assetIndicator = client.getAssetIndicator(tradeAsset);
                    assetIndicatorMap.put(assetIndicator.getSymbol(), assetIndicator);
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
                            Integer quantity = buyAmount
                                    .divide(BigDecimal.valueOf(assetIndicator.getPrice()), 0, RoundingMode.FLOOR)
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

    private boolean isBetweenStartAndEndTime(LocalTime time) {
        if(trade.getStartAt() == null || trade.getEndAt() == null) {
            return true;
        }
        if(time.isAfter(trade.getStartAt()) && time.isBefore(trade.getEndAt())) {
            return true;
        }
        return false;
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
