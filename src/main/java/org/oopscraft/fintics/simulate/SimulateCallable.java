package org.oopscraft.fintics.simulate;

import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.fintics.dao.TradeAssetOhlcvRepository;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.trade.TradeAssetDecider;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class SimulateCallable implements Callable<Simulate> {

    private final Simulate simulate;

    private final TradeAssetOhlcvRepository tradeAssetOhlcvRepository;

    public SimulateCallable(Simulate simulate, ApplicationContext applicationContext) {
        this.simulate = simulate;
        this.tradeAssetOhlcvRepository = applicationContext.getBean(TradeAssetOhlcvRepository.class);
    }

    @Override
    public Simulate call() throws Exception {
        Trade trade = simulate.getTrade();
        Balance balance = simulate.getBalance();
        LocalDateTime startAt = simulate.getStartAt();
        LocalDateTime endAt = simulate.getEndAt();
        int interval = trade.getInterval();

        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startAt.toInstant(ZoneOffset.UTC).toEpochMilli()), ZoneId.systemDefault());
        while(dateTime.isAfter(endAt)) {
            dateTime = dateTime.plusSeconds(interval);

            for(TradeAsset tradeAsset : trade.getTradeAssets()) {

                List<TradeAssetOhlcv> minuteOhlcvs = new ArrayList<>();
                List<TradeAssetOhlcv> dailyOhlcvs = new ArrayList<>();

                OrderBook orderBook = OrderBook.builder()
                        .build();

                TradeAssetIndicator tradeAssetIndicator = TradeAssetIndicator.builder()
                        .minuteOhlcvs(minuteOhlcvs)
                        .dailyOhlcvs(dailyOhlcvs)
                        .build();

                TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                        .dateTime(dateTime)
                        .tradeAssetIndicator(tradeAssetIndicator)
                        .build();

                Boolean result = tradeAssetDecider.execute();
                if(result == true) {
                    if(!balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                        BigDecimal buyAmount = balance.getTotalAmount()
                                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL32)
                                .multiply(tradeAsset.getHoldRatio())
                                .setScale(2, RoundingMode.HALF_UP);
                        BigDecimal askPrice = orderBook.getAskPrice();
                        BigDecimal quantity = buyAmount
                                .divide(askPrice, MathContext.DECIMAL32);
                        BigDecimal purchaseAmount = askPrice.multiply(quantity);

                        BalanceAsset balanceAsset = BalanceAsset.builder()
                                .symbol(tradeAsset.getSymbol())
                                .name(tradeAsset.getName())
                                .quantity(quantity)
                                .purchaseAmount(purchaseAmount)
                                .build();
                        balance.addBalanceAsset(balanceAsset);

                        Order order = Order.builder()
                                .orderId(IdGenerator.uuid())
                                .orderKind(OrderKind.BUY)
                                .tradeId(trade.getTradeId())
                                .symbol(tradeAsset.getSymbol())
                                .assetName(tradeAsset.getName())
                                .orderResult(OrderResult.COMPLETED)
                                .build();
                        simulate.addOrder(order);
                    }
                }
                if(result == false) {
                    if(balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                        BalanceAsset balanceAsset = balance.getBalanceAsset(tradeAsset.getSymbol()).orElseThrow();
                        BigDecimal quantity = balanceAsset.getQuantity();
                        BigDecimal purchaseAmount = balanceAsset.getPurchaseAmount();
                        BigDecimal bidPrice = orderBook.getBidPrice();
                        BigDecimal sellAmount = quantity.multiply(bidPrice);
                        BigDecimal profitAmount = purchaseAmount.subtract(sellAmount);
                        balance.setRealizedProfitAmount(balance.getRealizedProfitAmount().add(profitAmount));
                        balance.removeBalanceAsset(balanceAsset);
                    }
                }
            }


        }

        return this.simulate;
    }

}