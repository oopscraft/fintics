package org.oopscraft.fintics.trade.order.simple;

import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.trade.order.OrderOperator;
import org.oopscraft.fintics.trade.order.OrderOperatorContext;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class SimpleOrderOperator extends OrderOperator {

    public SimpleOrderOperator(OrderOperatorContext context) {
        super(context);
    }

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
