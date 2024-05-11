package org.oopscraft.fintics.simulate;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.dao.AssetOhlcvRepository;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class SimulateBrokerClient extends BrokerClient {

    private final AssetOhlcvRepository assetOhlcvRepository;

    private final BigDecimal minimumOrderQuantity;

    private final BigDecimal feeRate;

    private LocalDateTime dateTime;

    private final Map<String, List<Ohlcv>> minuteOhlcvsMap = new HashMap<>();

    private final Map<String, List<Ohlcv>> dailyOhlcvsMap = new HashMap<>();

    private final Balance balance = Balance.builder()
            .cashAmount(BigDecimal.ZERO)
            .build();

    @Getter
    private final List<Order> orders = new ArrayList<>();

    @Getter
    private final SimulateReport simulateReport = new SimulateReport();

    public void setDateTime(LocalDateTime dateTime) {
        if (this.dateTime != null) {
            simulateReport.snapshotBalance(this.dateTime, balance);
        }
        this.dateTime = dateTime;
        simulateReport.snapshotBalance(this.dateTime, balance);
    }

    @Builder
    protected SimulateBrokerClient(AssetOhlcvRepository assetOhlcvRepository, BigDecimal minimumOrderQuantity, BigDecimal feeRate) {
        super(null, new Properties());
        this.assetOhlcvRepository = assetOhlcvRepository;
        this.minimumOrderQuantity = minimumOrderQuantity;
        this.feeRate = feeRate;
    }

    public synchronized void deposit(BigDecimal amount) {
        balance.setCashAmount(balance.getCashAmount().add(amount));
        simulateReport.snapshotBalance(dateTime, balance);
    }

    public synchronized void withdraw(BigDecimal amount) {
        if (amount.compareTo(balance.getCashAmount()) > 0) {
            throw new RuntimeException("withdraw amount is over cache amount");
        }
        balance.setCashAmount(balance.getCashAmount().subtract(amount));
        simulateReport.snapshotBalance(dateTime, balance);
    }

    public synchronized void deductFee(BigDecimal amount) {
        BigDecimal feeAmount = amount
                .multiply(feeRate.divide(BigDecimal.valueOf(100), MathContext.DECIMAL32))
                .setScale(2, RoundingMode.CEILING);
        balance.setCashAmount(balance.getCashAmount().subtract(feeAmount));
        simulateReport.snapshotBalance(dateTime, balance);
    }

    private void loadOhlcvsIfNotExist(Asset asset, LocalDateTime dateTime) {
        // minutes
        List<Ohlcv> minuteOhlcvs = minuteOhlcvsMap.get(asset.getAssetId());
        if(minuteOhlcvs == null || minuteOhlcvs.isEmpty() || minuteOhlcvs.get(0).getDateTime().isBefore(dateTime)) {
            minuteOhlcvs = assetOhlcvRepository.findAllByAssetIdAndType(asset.getAssetId(), Ohlcv.Type.MINUTE, dateTime.minusMonths(1), dateTime.plusMonths(1), Pageable.unpaged())
                    .stream()
                    .map(Ohlcv::from)
                    .toList();
            minuteOhlcvsMap.put(asset.getAssetId(), minuteOhlcvs);
        }

        // daily
        List<Ohlcv> dailyOhlcvs = dailyOhlcvsMap.get(asset.getAssetId());
        if(dailyOhlcvs == null || dailyOhlcvs.isEmpty() || dailyOhlcvs.get(0).getDateTime().isBefore(dateTime)) {
            dailyOhlcvs = assetOhlcvRepository.findAllByAssetIdAndType(asset.getAssetId(), Ohlcv.Type.DAILY, dateTime.minusYears(1), dateTime.plusYears(1), Pageable.unpaged())
                    .stream()
                    .map(Ohlcv::from)
                    .toList();
            dailyOhlcvsMap.put(asset.getAssetId(), dailyOhlcvs);
        }
    }

    @Override
    public boolean isOpened(LocalDateTime dateTime) throws InterruptedException {
        return true;
    }

    @Override
    public List<Asset> getAssets() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Ohlcv> getMinuteOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        loadOhlcvsIfNotExist(asset, dateTime);
        LocalDateTime dateTimeFrom = dateTime.minusWeeks(1);
        LocalDateTime dateTimeTo = dateTime.minusMinutes(0);
        return minuteOhlcvsMap.get(asset.getAssetId()).stream()
                .filter(ohlcv -> (ohlcv.getDateTime().isAfter(dateTimeFrom) || ohlcv.getDateTime().isEqual(dateTimeFrom))
                        && (ohlcv.getDateTime().isBefore(dateTimeTo) || ohlcv.getDateTime().isEqual(dateTimeTo)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        loadOhlcvsIfNotExist(asset, dateTime);
        LocalDateTime dateTimeFrom = dateTime.minusYears(1);
        LocalDateTime dateTimeTo = dateTime.minusDays(0);
        List<Ohlcv> dailyOhlcvs = dailyOhlcvsMap.get(asset.getAssetId()).stream()
                .filter(ohlcv -> (ohlcv.getDateTime().isAfter(dateTimeFrom) || ohlcv.getDateTime().isEqual(dateTimeFrom))
                        && (ohlcv.getDateTime().isBefore(dateTimeTo) || ohlcv.getDateTime().isEqual(dateTimeTo)))
                .collect(Collectors.toList());

        // 저정된 이전 일봉 데이터는 그날 장종료 후 데이터 이므로 현재 분봉의 데이터를 반영
        List<Ohlcv> todayMinuteOhlcvs = getMinuteOhlcvs(asset, dateTime).stream()
                .filter(ohlcv -> ohlcv.getDateTime().toLocalDate().isEqual(dateTime.toLocalDate()))
                .collect(Collectors.toList());
        BigDecimal highPrice = todayMinuteOhlcvs.stream()
                .map(Ohlcv::getHighPrice)
                .reduce(BigDecimal::max)
                .orElseThrow();
        BigDecimal lowPrice = todayMinuteOhlcvs.stream()
                .map(Ohlcv::getLowPrice)
                .reduce(BigDecimal::min)
                .orElseThrow();
        BigDecimal closePrice = todayMinuteOhlcvs.get(0).getClosePrice();
        BigDecimal volume = todayMinuteOhlcvs.stream()
                .map(Ohlcv::getVolume)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Ohlcv todayDailyOhlcv = dailyOhlcvs.get(0);
        todayDailyOhlcv.setHighPrice(highPrice);
        todayDailyOhlcv.setLowPrice(lowPrice);
        todayDailyOhlcv.setClosePrice(closePrice);
        todayDailyOhlcv.setVolume(volume);

        // return
        return dailyOhlcvs;
    }

    @Override
    public BigDecimal getTickPrice(Asset asset, BigDecimal price) throws InterruptedException {
        return null;
    }

    @Override
    public BigDecimal getMinimumOrderQuantity() throws InterruptedException {
        return this.minimumOrderQuantity;
    }

    @Override
    public OrderBook getOrderBook(Asset asset) {
        loadOhlcvsIfNotExist(asset, dateTime);
        LocalDateTime dateTimeTo = dateTime.truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime dateTimeFrom = dateTimeTo.minusMinutes(1);
        List<Ohlcv> minuteOhlcvs = minuteOhlcvsMap.get(asset.getAssetId());
        Ohlcv minuteOhlcv = minuteOhlcvs.stream()
                .filter(assetOhlcv -> (assetOhlcv.getDateTime().isAfter(dateTimeFrom) || assetOhlcv.getDateTime().isEqual(dateTimeFrom))
                        && (assetOhlcv.getDateTime().isBefore(dateTimeTo) || assetOhlcv.getDateTime().isEqual(dateTimeTo)))
                .findFirst()
                .orElse(null);
        BigDecimal price = minuteOhlcv.getClosePrice();
        BigDecimal bidPrice = minuteOhlcv.getClosePrice();
        BigDecimal askPrice = minuteOhlcv.getClosePrice();
        return OrderBook.builder()
                .price(price)
                .bidPrice(bidPrice)
                .askPrice(askPrice)
                .build();
    }

    @Override
    public Balance getBalance() {
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
    public Order submitOrder(Asset asset, Order order) throws InterruptedException {
        order.setOrderAt(dateTime);

        // order book
        OrderBook orderBook = getOrderBook(asset);

        // balance
        Balance balance = getBalance();

        // balance asset
        BalanceAsset balanceAsset = balance.getBalanceAsset(order.getAssetId()).orElse(null);

        // buy
        if(order.getType() == Order.Type.BUY) {
            BigDecimal buyQuantity = order.getQuantity();

            // minimum order quantity
            if (buyQuantity.compareTo(minimumOrderQuantity) < 0) {
                throw new RuntimeException(String.format("[%s] is under minimum order quantity", buyQuantity));
            }
            if (minimumOrderQuantity.compareTo(BigDecimal.ZERO) > 0) {
                buyQuantity = buyQuantity.divide(minimumOrderQuantity, MathContext.DECIMAL32)
                        .setScale(0, RoundingMode.FLOOR)
                        .multiply(minimumOrderQuantity);
            }

            // buy price, amount
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
                BigDecimal quantity = balanceAsset.getQuantity().add(buyQuantity);
                BigDecimal purchaseAmount = balanceAsset.getPurchaseAmount().add(buyAmount);
                BigDecimal purchasePrice = purchaseAmount.divide(quantity, MathContext.DECIMAL32);
                balanceAsset.setQuantity(quantity);
                balanceAsset.setOrderableQuantity(quantity);
                balanceAsset.setPurchaseAmount(purchaseAmount);
                balanceAsset.setPurchasePrice(purchasePrice);
            }

            // deduct fee
            deductFee(buyAmount);
        }

        // sell
        if(order.getType() == Order.Type.SELL) {
            Objects.requireNonNull(balanceAsset, "balance asset is null");
            BigDecimal sellQuantity = order.getQuantity();

            // check minimum order quantity
            if (sellQuantity.compareTo(minimumOrderQuantity) < 0) {
                throw new RuntimeException(String.format("[%s] is under minimum order quantity", sellQuantity));
            }
            if (minimumOrderQuantity.compareTo(BigDecimal.ZERO) > 0) {
                sellQuantity = sellQuantity.divide(minimumOrderQuantity, MathContext.DECIMAL32)
                        .setScale(0, RoundingMode.FLOOR)
                        .multiply(minimumOrderQuantity);
            }

            // sell price, amount
            BigDecimal sellPrice = orderBook.getBidPrice();
            BigDecimal sellAmount = sellQuantity.multiply(sellPrice, MathContext.DECIMAL32);

            BigDecimal quantity = balanceAsset.getQuantity().subtract(sellQuantity);
            if(quantity.compareTo(BigDecimal.ZERO) <= 0) {
                balance.getBalanceAssets().remove(balanceAsset);
            }else{
                BigDecimal purchaseAmount = balanceAsset.getPurchaseAmount().subtract(sellAmount);
                BigDecimal purchasePrice = purchaseAmount.divide(quantity, MathContext.DECIMAL32);
                balanceAsset.setQuantity(quantity);
                balanceAsset.setPurchasePrice(purchasePrice);
                balanceAsset.setPurchaseAmount(purchaseAmount);
            }

            // deposit
            deposit(sellAmount);

            // deduct fee
            deductFee(sellAmount);
        }

        // save order
        orders.add(order);

        // snapshot
        simulateReport.snapshotBalance(dateTime, balance);

        // return
        return order;
    }

    @Override
    public List<Order> getWaitingOrders() throws InterruptedException {
        return new ArrayList<>();
    }

    @Override
    public Order amendOrder(Asset asset, Order order) throws InterruptedException {
        throw new UnsupportedOperationException("amend order is not supported.");
    }

}
