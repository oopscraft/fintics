package org.oopscraft.fintics.trade.order.rebalance;

import org.oopscraft.fintics.model.BalanceAsset;
import org.oopscraft.fintics.model.TradeAsset;
import org.oopscraft.fintics.trade.order.OrderOperator;
import org.oopscraft.fintics.trade.order.OrderOperatorContext;

import java.math.BigDecimal;
import java.math.MathContext;

public class RebalanceOrderOperator extends OrderOperator {

    public RebalanceOrderOperator(OrderOperatorContext context) {
        super(context);
    }

    @Override
    public void buyTradeAsset(TradeAsset tradeAsset) throws InterruptedException {
        BigDecimal holdRatioAmount = calculateHoldRatioAmount(tradeAsset);
        BigDecimal valuationAmount = getBalance().getBalanceAsset(tradeAsset.getAssetId())
                .map(BalanceAsset::getValuationAmount)
                .orElse(BigDecimal.ZERO);

        BigDecimal buyAmount = holdRatioAmount.subtract(valuationAmount);
        if(buyAmount.compareTo(BigDecimal.ZERO) > 0) {
            buyAssetByAmount(tradeAsset, buyAmount);
        }
    }

    @Override
    public void sellTradeAsset(TradeAsset tradeAsset) throws InterruptedException {
        BigDecimal holdRatioAmount = calculateHoldRatioAmount(tradeAsset);
        BigDecimal valuationAmount = getBalance().getBalanceAsset(tradeAsset.getAssetId())
                .map(BalanceAsset::getValuationAmount)
                .orElse(BigDecimal.ZERO);

        BigDecimal sellAmount = valuationAmount.subtract(holdRatioAmount);
        if(sellAmount.compareTo(BigDecimal.ZERO) > 0) {
            sellAssetByAmount(tradeAsset, sellAmount);
        }
    }

}
