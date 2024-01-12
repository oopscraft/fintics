package org.oopscraft.fintics.simulate;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SimulateTradeClient extends TradeClient {

    private final Balance balance = Balance.builder()
            .cashAmount(BigDecimal.ZERO)
            .build();

    @Setter
    @Getter
    private LocalDateTime dateTime = LocalDateTime.now();

    private final Map<String,List<Ohlcv>> minuteOhlcvsMap = new HashMap<>();

    private final Map<String,List<Ohlcv>> dailyOhlcvsMap = new HashMap<>();

    @Getter
    private final List<Order> orders = new ArrayList<>();

    public SimulateTradeClient() {
        super(new Properties());
    }

    public void deposit(BigDecimal amount) {
        balance.setCashAmount(balance.getCashAmount().add(amount));
    }

    public void withdraw(BigDecimal amount) {
        balance.setCashAmount(balance.getCashAmount().subtract(amount));
    }

    public void addMinuteOhlcvs(Asset asset, List<Ohlcv> minuteOhlcvs) {
        minuteOhlcvsMap.put(asset.getAssetId(), minuteOhlcvs);
    }

    public void addDailyOhlcvs(Asset asset, List<Ohlcv> dailyOhlcvs) {
        dailyOhlcvsMap.put(asset.getAssetId(), dailyOhlcvs);
    }

    @Override
    public boolean isOpened(LocalDateTime dateTime) throws InterruptedException {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    @Override
    public List<Ohlcv> getMinuteOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        Objects.requireNonNull(minuteOhlcvsMap.get(asset.getAssetId()));
        LocalDateTime dateTimeFrom = dateTime.minusWeeks(1);
        LocalDateTime dateTimeTo = dateTime.minusMinutes(0);
        return minuteOhlcvsMap.get(asset.getAssetId()).stream()
                .filter(ohlcv -> (ohlcv.getDateTime().isAfter(dateTimeFrom) || ohlcv.getDateTime().isEqual(dateTimeFrom))
                        && (ohlcv.getDateTime().isBefore(dateTimeTo) || ohlcv.getDateTime().isEqual(dateTimeTo)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        Objects.requireNonNull(dailyOhlcvsMap.get(asset.getAssetId()));
        LocalDateTime dateTimeFrom = dateTime.minusYears(1);
        LocalDateTime dateTimeTo = dateTime.minusDays(0);
        return dailyOhlcvsMap.get(asset.getAssetId()).stream()
                .filter(ohlcv -> (ohlcv.getDateTime().isAfter(dateTimeFrom) || ohlcv.getDateTime().isEqual(dateTimeFrom))
                        && (ohlcv.getDateTime().isBefore(dateTimeTo) || ohlcv.getDateTime().isEqual(dateTimeTo)))
                .collect(Collectors.toList());
    }

    @Override
    public OrderBook getOrderBook(Asset asset) throws InterruptedException {
        Objects.requireNonNull(minuteOhlcvsMap.get(asset.getAssetId()));
        Ohlcv ohlcv = minuteOhlcvsMap.get(asset.getAssetId()).stream()
                .filter(el -> el.getDateTime().isEqual(dateTime))
                .findFirst()
                .orElseThrow();
        BigDecimal price = ohlcv.getClosePrice();
        BigDecimal bidPrice = ohlcv.getLowPrice();
        BigDecimal askPrice = ohlcv.getHighPrice();
        return OrderBook.builder()
                .price(price)
                .bidPrice(bidPrice)
                .askPrice(askPrice)
                .build();
    }

    @Override
    public Balance getBalance() throws InterruptedException {
        BigDecimal totalPurchaseAmount = BigDecimal.ZERO;
        BigDecimal totalValuationAmount = BigDecimal.ZERO;
        BigDecimal totalProfitAmount = BigDecimal.ZERO;
        for(BalanceAsset balanceAsset : balance.getBalanceAssets()) {
            OrderBook orderBook = getOrderBook(balanceAsset);
            BigDecimal currentPrice = orderBook.getPrice();
            BigDecimal purchaseAmount = balanceAsset.getPurchaseAmount();
            BigDecimal valuationAmount = balanceAsset.getQuantity().multiply(currentPrice, MathContext.DECIMAL32);
            BigDecimal profitAmount = valuationAmount.subtract(purchaseAmount);

            balanceAsset.setPurchaseAmount(purchaseAmount);
            balanceAsset.setValuationAmount(valuationAmount);
            balanceAsset.setProfitAmount(profitAmount);

            totalPurchaseAmount = totalPurchaseAmount.add(purchaseAmount);
            totalValuationAmount = totalValuationAmount.add(valuationAmount);
            totalProfitAmount = totalProfitAmount.add(profitAmount);
        }

        BigDecimal totalAmount = totalValuationAmount.add(balance.getCashAmount());
        balance.setTotalAmount(totalAmount);
        balance.setPurchaseAmount(totalPurchaseAmount);
        balance.setValuationAmount(totalValuationAmount);
        balance.setProfitAmount(totalProfitAmount);
        return balance;
    }

    @Override
    public Order submitOrder(Order order) throws InterruptedException {
        String assetId = order.getAssetId();
        String assetName = order.getAssetName();

        // order book
        Asset asset = Asset.builder()
                .assetId(assetId)
                .assetName(assetName)
                .build();
        OrderBook orderBook = getOrderBook(asset);

        // balance
        Balance balance = getBalance();

        // balance asset
        BalanceAsset balanceAsset = balance.getBalanceAsset(order.getAssetId()).orElse(null);

        // buy
        if(order.getOrderType() == OrderType.BUY) {
            BigDecimal buyQuantity = order.getQuantity();
            BigDecimal buyPrice = orderBook.getAskPrice();
            BigDecimal buyAmount = buyQuantity.multiply(buyPrice, MathContext.DECIMAL32);

            // withdraw
            withdraw(buyAmount);

            if(balanceAsset == null) {
                balanceAsset = BalanceAsset.builder()
                        .assetId(order.getAssetId())
                        .assetName(order.getAssetName())
                        .quantity(buyQuantity)
                        .orderableQuantity(buyQuantity)
                        .purchasePrice(buyPrice)
                        .purchaseAmount(buyAmount)
                        .build();
                balance.getBalanceAssets().add(balanceAsset);
            }else{
                BigDecimal holdQuantity = balanceAsset.getQuantity().add(buyQuantity);
                BigDecimal holdPurchaseAmount = balanceAsset.getPurchaseAmount().add(buyAmount);
                BigDecimal holdBuyPurchasePrice = holdPurchaseAmount.divide(holdQuantity, MathContext.DECIMAL32);
                balanceAsset.setQuantity(holdQuantity);
                balanceAsset.setOrderableQuantity(holdQuantity);
                balanceAsset.setPurchasePrice(holdBuyPurchasePrice);
                balanceAsset.setPurchaseAmount(holdPurchaseAmount);
            }
        }

        // sell
        if(order.getOrderType() == OrderType.SELL) {
            Objects.requireNonNull(balanceAsset, "balance asset is null");
            BigDecimal sellQuantity = order.getQuantity();
            BigDecimal sellPrice = orderBook.getBidPrice();
            BigDecimal sellAmount = sellQuantity.multiply(sellPrice, MathContext.DECIMAL32);
            if(sellQuantity.compareTo(balanceAsset.getQuantity()) > 0) {
                throw new RuntimeException("quantity exceeded the held quantity");
            }
            BigDecimal holdQuantity = balanceAsset.getQuantity().subtract(sellQuantity);
            if(holdQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                balance.getBalanceAssets().remove(balanceAsset);
            }else{
                BigDecimal holdPurchaseAmount = balanceAsset.getPurchaseAmount().subtract(sellAmount);
                BigDecimal holdPurchasePrice = holdPurchaseAmount.divide(holdQuantity, MathContext.DECIMAL32);
                balanceAsset.setQuantity(holdQuantity);
                balanceAsset.setPurchasePrice(holdPurchasePrice);
                balanceAsset.setPurchaseAmount(holdPurchaseAmount);
            }

            // deposit
            deposit(sellAmount);
        }

        // save order
        orders.add(order);

        // return
        return order;
    }

    @Override
    public List<Order> getWaitingOrders() throws InterruptedException {
        return new ArrayList<>();
    }

    @Override
    public Order amendOrder(Order order) throws InterruptedException {
        throw new UnsupportedOperationException("amend order is not supported.");
    }

}
