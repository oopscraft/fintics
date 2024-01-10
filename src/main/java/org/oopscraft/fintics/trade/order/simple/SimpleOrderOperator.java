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
            BigDecimal buyAmount = getBalance().getTotalAmount()
                    .divide(BigDecimal.valueOf(100), MathContext.DECIMAL32)
                    .multiply(tradeAsset.getHoldRatio())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal price = getOrderBook().getPrice();
            BigDecimal quantity = buyAmount
                    .divide(price, MathContext.DECIMAL32);

            // buy
            log.info("Buy asset: {}", tradeAsset.getAssetName());
            buyAsset(tradeAsset, quantity, price);
        }
    }

    @Override
    public void sellTradeAsset(TradeAsset tradeAsset) throws InterruptedException {
        if (getBalance().hasBalanceAsset(tradeAsset.getAssetId())) {
            // price, quantity
            BalanceAsset balanceAsset = getBalance().getBalanceAsset(tradeAsset.getAssetId()).orElseThrow();
            BigDecimal price = getOrderBook().getPrice();
            BigDecimal quantity = balanceAsset.getOrderableQuantity();

            // sell
            log.info("Sell asset: {}", balanceAsset.getAssetName());
            sellAsset(tradeAsset, quantity, price);
        }
    }

}
