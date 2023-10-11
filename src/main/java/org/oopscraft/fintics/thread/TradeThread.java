package org.oopscraft.fintics.thread;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.Client;
import org.oopscraft.fintics.client.ClientFactory;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TradeThread extends Thread {

    @Getter
    private final Trade trade;

    @Getter
    private final Map<String, AssetIndicator> assetIndicatorMap = new ConcurrentHashMap<>();

    public TradeThread(Trade trade) {
        this.trade = trade;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            try {
                Thread.sleep(trade.getInterval() * 1_000);

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

                    // 1. null is no operation
                    if(holdConditionResult == null) {
                        continue;
                    }

                    // 2. buy and hold
                    if(holdConditionResult.equals(Boolean.TRUE)) {
                        if(!balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                            BigDecimal buyAmount = balance.getTotalAmount().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                                            .multiply(tradeAsset.getHoldRatio());
                            BigDecimal quantity = buyAmount.divide(assetIndicator.getPrice(), 0, RoundingMode.FLOOR);
                            try {
                                client.buyAsset(tradeAsset, quantity.intValue());
                            }catch(Throwable e) {
                                log.warn(e.getMessage());
                            }
                        }
                    }

                    // 3. sell
                    else if(holdConditionResult.equals(Boolean.FALSE)) {
                        if(balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                            BalanceAsset balanceAsset = balance.getBalanceAsset(tradeAsset.getSymbol());
                            BigDecimal quantity = balanceAsset.getQuantity();
                            try {
                                client.sellAsset(balanceAsset, quantity.intValue());
                            }catch(Throwable e) {
                                log.warn(e.getMessage());
                            }
                        }
                    }
                }

            } catch (InterruptedException e) {
                log.warn(e.getMessage());
                break;
            } catch (Throwable t) {
                log.warn(t.getMessage());
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

}
