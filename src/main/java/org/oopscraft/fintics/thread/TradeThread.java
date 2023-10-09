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

@Slf4j
public class TradeThread extends Thread {

    @Getter
    private final Trade trade;


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
                    AssetIndicator assetIndicator = client.getAssetIndicator(tradeAsset.getSymbol(), tradeAsset.getType());
                    Boolean holdConditionResult = getHoldConditionResult(assetIndicator);

                    // no op
                    if(holdConditionResult == null) {
                        continue;
                    }

                    // buy and hold
                    if(holdConditionResult.equals(Boolean.TRUE)) {
                        if(!balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                            BigDecimal buyAmount = balance.getTotal().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                                            .multiply(tradeAsset.getHoldRatio());
                            BigDecimal price = assetIndicator.getPrice();
                            BigDecimal quantity = buyAmount.divide(assetIndicator.getPrice(), 0, RoundingMode.FLOOR);
                            client.buyAsset(tradeAsset, price, quantity.intValue());
                        }
                    }

                    // sell
                    else if(holdConditionResult.equals(Boolean.FALSE)) {
                        if(balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                            BalanceAsset balanceAsset = balance.getBalanceAsset(tradeAsset.getSymbol());
                            BigDecimal price = assetIndicator.getPrice();
                            BigDecimal quantity = balanceAsset.getQuantity();
                            client.sellAsset(balanceAsset, price, quantity.intValue());
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
        Object result = groovyShell.evaluate(script);
        if(result == null) {
            return null;
        }
        return (Boolean) result;
    }

}
