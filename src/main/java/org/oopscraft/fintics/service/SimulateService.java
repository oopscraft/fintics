package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.trade.TradeAssetDecider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulateService {

    public Simulate simulate(Simulate simulate) {
        String holdCondition = simulate.getHoldCondition();
        LocalTime startAt = simulate.getStartAt();
        LocalTime endAt = simulate.getEndAt();
        List<Ohlcv> minuteOhlcvs = simulate.getMinuteOhlcvs();
        List<Ohlcv> dailyOhlcvs = simulate.getDailyOhlcvs();
        Double feeRate = simulate.getFeeRate();
        Double bidAskSpread = simulate.getBidAskSpread();
        LocalDateTime dateTimeFrom = simulate.getDateTimeFrom();
        LocalDateTime dateTimeTo = simulate.getDateTimeTo();
        BigDecimal investAmount = simulate.getInvestAmount();
        Balance balance = simulate.getBalance();
        List<Order> orders = simulate.getOrders();

        balance.setTotalAmount(investAmount);
        for(LocalDateTime dateTime = dateTimeFrom.plusMinutes(1); dateTime.isBefore(dateTimeTo); dateTime = dateTime.plusMinutes(1)) {
            // check startAt, endAt time
            LocalTime time = dateTime.toLocalTime();
            if(startAt != null && endAt != null) {
                if(time.isBefore(startAt) || time.isAfter(endAt)) {
                    continue;
                }
            }
            log.debug("== dateTime:{}", dateTime);

            List<Ohlcv> currentMinuteOhlcvs = subtractCurrentMinuteOhlcvs(minuteOhlcvs, dateTime);
            log.debug("currentMinuteOhlcv:{}", currentMinuteOhlcvs.size());

            List<Ohlcv> currentDailyOhlcvs = subtractCurrentDailyOhlcvs(dailyOhlcvs, dateTime);
            log.debug("currentDailyOhlcv:{}", currentDailyOhlcvs);

            BigDecimal closePrice = currentMinuteOhlcvs.get(0).getClosePrice();

            Trade trade = Trade.builder()
                    .id("simulate")
                    .holdCondition(simulate.getHoldCondition())
                    .build();

            TradeAsset tradeAsset = TradeAsset.builder()
                    .tradeId(trade.getId())
                    .name("simulate TradeAsset")
                    .build();

            AssetIndicator assetIndicator = AssetIndicator.builder()
                    .id(tradeAsset.getId())
                    .name(tradeAsset.getName())
                    .minuteOhlcvs(currentMinuteOhlcvs)
                    .dailyOhlcvs(currentDailyOhlcvs)
                    .build();

            TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                    .holdCondition(holdCondition)
                    .dateTime(dateTime)
                    .balance(balance)
                    .indiceIndicators(new ArrayList<IndiceIndicator>())
                    .assetIndicator(assetIndicator)
                    .build();
            Boolean holdConditionResult = tradeAssetDecider.execute();
            log.info("[{}] holdConditionResult: {}", dateTime, holdConditionResult);

            if(holdConditionResult != null) {
                if (holdConditionResult) {
                    if(!balance.hasBalanceAsset(tradeAsset.getId())) {
                        BigDecimal buyAmount = balance.getTotalAmount()
                                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL32)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);
                        BigDecimal askPrice = closePrice
                                .add(BigDecimal.valueOf(bidAskSpread));
                        BigDecimal quantity = buyAmount
                                .divide(askPrice, MathContext.DECIMAL32);
                        BigDecimal purchaseAmount = askPrice.multiply(quantity);
                        BalanceAsset balanceAsset = BalanceAsset.builder()
                                .quantity(quantity)
                                .orderableQuantity(quantity)
                                .purchasePrice(askPrice)
                                .purchaseAmount(purchaseAmount)
                                .build();
                        balance.addBalanceAsset(balanceAsset);

                        // add order
                        Order order = Order.builder()
                                .id(IdGenerator.uuid())
                                .orderAt(dateTime)
                                .orderType(OrderType.BUY)
                                .price(askPrice)
                                .quantity(quantity)
                                .build();
                        orders.add(order);
                    }
                }
                if (!holdConditionResult) {
                    if(balance.hasBalanceAsset(tradeAsset.getId())) {
                        BalanceAsset balanceAsset = balance.getBalanceAsset(tradeAsset.getId()).orElseThrow();
                        BigDecimal bidPrice = closePrice.subtract(BigDecimal.valueOf(bidAskSpread));
                        BigDecimal quantity = balanceAsset.getQuantity();
                        BigDecimal purchaseAmount = balanceAsset.getPurchaseAmount();
                        BigDecimal sellAmount = bidPrice.multiply(quantity);
                        BigDecimal profitAmount = sellAmount.subtract(purchaseAmount);
                        balance.setRealizedProfitAmount(balance.getRealizedProfitAmount().add(profitAmount));
                        balance.removeBalanceAsset(balanceAsset);

                        // add order
                        Order order = Order.builder()
                                .id(IdGenerator.uuid())
                                .orderAt(dateTime)
                                .orderType(OrderType.SELL)
                                .price(bidPrice)
                                .quantity(quantity)
                                .build();
                        orders.add(order);
                    }
                }

                // updates valuation amount
                if(balance.hasBalanceAsset(tradeAsset.getId())) {
                    BalanceAsset balanceAsset = balance.getBalanceAsset(tradeAsset.getId()).orElseThrow();
                    balanceAsset.setValuationAmount(balanceAsset.getQuantity().multiply(closePrice));
                    balance.setValuationAmount(balanceAsset.getValuationAmount());
                }else{
                    balance.setValuationAmount(BigDecimal.ZERO);
                }
            }
        }

        // return
        return simulate;
    }

    private List<Ohlcv> subtractCurrentMinuteOhlcvs(List<Ohlcv> minuteOhlcvs, LocalDateTime dateTime) {
        return minuteOhlcvs.stream()
                .filter(ohlcv ->
                    (ohlcv.getDateTime().isEqual(dateTime) || ohlcv.getDateTime().isBefore(dateTime))
                    && ohlcv.getDateTime().isAfter(dateTime.minusWeeks(1))
                )
                .limit(1000)
                .collect(Collectors.toList());
    }

    private List<Ohlcv> subtractCurrentDailyOhlcvs(List<Ohlcv> dailyOhlcvs, LocalDateTime dateTime) {
        return dailyOhlcvs.stream()
                .filter(ohlcv ->
                        (ohlcv.getDateTime().isEqual(dateTime) || ohlcv.getDateTime().isBefore(dateTime))
                                && ohlcv.getDateTime().isAfter(dateTime.minusYears(10))
                )
                .limit(1000)
                .collect(Collectors.toList());
    }


    private Double calculateFee(Double price, Double feeRate) {
        double fee = BigDecimal.valueOf(price)
                .multiply(BigDecimal.valueOf(feeRate).divide(BigDecimal.valueOf(100), MathContext.DECIMAL128))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        return fee;
    }


}
