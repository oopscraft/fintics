package org.oopscraft.fintics.trade.order.simple;

import org.oopscraft.fintics.model.BalanceAsset;
import org.oopscraft.fintics.model.TradeAsset;
import org.oopscraft.fintics.trade.order.OrderOperator;

import java.math.BigDecimal;

public class SimpleOrderOperator extends OrderOperator {

    @Override
    public void buyTradeAsset(TradeAsset tradeAsset) throws InterruptedException {
        if (!getBalance().hasBalanceAsset(tradeAsset.getAssetId())) {
            BigDecimal holdRatioAmount = calculateHoldRatioAmount(tradeAsset);
            buyAssetByAmount(tradeAsset, holdRatioAmount);
        }
    }

    @Override
    public void sellTradeAsset(TradeAsset tradeAsset) throws InterruptedException {
        if (getBalance().hasBalanceAsset(tradeAsset.getAssetId())) {
            BalanceAsset balanceAsset = getBalance().getBalanceAsset(tradeAsset.getAssetId()).orElseThrow();
            BigDecimal quantity = balanceAsset.getOrderableQuantity();
            BigDecimal price = getOrderBook().getPrice();
            sellAssetByQuantityAndPrice(tradeAsset, quantity, price);
        }
    }

}
