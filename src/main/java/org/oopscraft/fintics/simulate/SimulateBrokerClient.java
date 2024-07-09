package org.oopscraft.fintics.simulate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.dao.OhlcvRepository;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class SimulateBrokerClient extends BrokerClient {

    private final OhlcvRepository assetOhlcvRepository;

    private final ZoneId timezone;

    private final BigDecimal feeRate;

    @Setter
    private LocalDateTime datetime;

    private final Map<String, List<Ohlcv>> minuteOhlcvsMap = new HashMap<>();

    private final Map<String, List<Ohlcv>> dailyOhlcvsMap = new HashMap<>();

    private BigDecimal balanceCashAmount = BigDecimal.ZERO;

    private final List<BalanceAsset> balanceAssets = new CopyOnWriteArrayList<>();

    @Getter
    private final List<Order> orders = new ArrayList<>();

    @Getter
    private final SimulateReport simulateReport = new SimulateReport();

    @Builder
    protected SimulateBrokerClient(OhlcvRepository assetOhlcvRepository, ZoneId timezone, BigDecimal feeRate) {
        super(null, new Properties());
        this.assetOhlcvRepository = assetOhlcvRepository;
        this.timezone = timezone;
        this.feeRate = feeRate;
    }

    public synchronized void deposit(BigDecimal amount) {
        balanceCashAmount = balanceCashAmount.add(amount);
        snapshotSimulateReport();
    }

    public synchronized void withdraw(BigDecimal amount) {
        if (amount.compareTo(balanceCashAmount) > 0) {
            throw new RuntimeException("withdraw amount is over cache amount");
        }
        balanceCashAmount = balanceCashAmount.subtract(amount);
        snapshotSimulateReport();
    }

    public synchronized void deductFee(BigDecimal amount) {
        BigDecimal feeAmount = getFeeAmount(amount);
        balanceCashAmount = balanceCashAmount.subtract(feeAmount);
        simulateReport.addFeeAmount(feeAmount);
        snapshotSimulateReport();
    }

    public BigDecimal getFeeAmount(BigDecimal amount) {
        return amount.multiply(feeRate.divide(BigDecimal.valueOf(100), MathContext.DECIMAL32))
                .setScale(2, RoundingMode.CEILING);
    }

    private void loadOhlcvsIfNotExist(Asset asset, LocalDateTime datetime) {
        // minutes
        List<Ohlcv> minuteOhlcvs = minuteOhlcvsMap.get(asset.getAssetId());
        if(minuteOhlcvs == null || minuteOhlcvs.isEmpty() || minuteOhlcvs.get(0).getDateTime().isBefore(datetime)) {
            LocalDateTime datetimeFrom = datetime.minusMonths(1);
            LocalDateTime datetimeTo = datetime.plusMonths(1);
            minuteOhlcvs = assetOhlcvRepository.findAllByAssetIdAndType(asset.getAssetId(), Ohlcv.Type.MINUTE, datetimeFrom, datetimeTo, Pageable.unpaged())
                    .stream()
                    .map(Ohlcv::from)
                    .toList();
            minuteOhlcvsMap.put(asset.getAssetId(), minuteOhlcvs);
        }

        // daily
        List<Ohlcv> dailyOhlcvs = dailyOhlcvsMap.get(asset.getAssetId());
        if(dailyOhlcvs == null || dailyOhlcvs.isEmpty() || dailyOhlcvs.get(0).getDateTime().isBefore(datetime)) {
            LocalDateTime datetimeFrom = datetime.minusYears(1);
            LocalDateTime datetimeTo = datetime.plusYears(1);
            dailyOhlcvs = assetOhlcvRepository.findAllByAssetIdAndType(asset.getAssetId(), Ohlcv.Type.DAILY, datetimeFrom, datetimeTo, Pageable.unpaged())
                    .stream()
                    .map(Ohlcv::from)
                    .toList();
            dailyOhlcvsMap.put(asset.getAssetId(), dailyOhlcvs);
        }
    }

    @Override
    public boolean isOpened(LocalDateTime datetime) throws InterruptedException {
        return true;
    }

    @Override
    public List<Ohlcv> getMinuteOhlcvs(Asset asset) throws InterruptedException {
        loadOhlcvsIfNotExist(asset, datetime);
        LocalDateTime datetimeFrom = datetime.minusWeeks(1);
        LocalDateTime datetimeTo = datetime;
        return minuteOhlcvsMap.get(asset.getAssetId()).stream()
                .filter(ohlcv -> (ohlcv.getDateTime().isAfter(datetimeFrom) || ohlcv.getDateTime().equals(datetimeFrom))
                        && (ohlcv.getDateTime().isBefore(datetimeTo) || ohlcv.getDateTime().equals(datetimeTo)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(Asset asset) throws InterruptedException {
        loadOhlcvsIfNotExist(asset, datetime);
        LocalDateTime datetimeFrom = datetime.minusYears(1);
        LocalDateTime datetimeTo = datetime;
        List<Ohlcv> dailyOhlcvs = dailyOhlcvsMap.get(asset.getAssetId()).stream()
                .filter(ohlcv -> (ohlcv.getDateTime().isAfter(datetimeFrom) || ohlcv.getDateTime().equals(datetimeFrom))
                        && (ohlcv.getDateTime().isBefore(datetimeTo) || ohlcv.getDateTime().equals(datetimeTo)))
                .collect(Collectors.toList());

        // 저정된 이전 일봉 데이터는 그날 장종료 후 데이터 이므로 현재 분봉의 데이터를 반영
        List<Ohlcv> todayMinuteOhlcvs = getMinuteOhlcvs(asset).stream()
                .filter(ohlcv -> ohlcv.getDateTime().truncatedTo(ChronoUnit.DAYS).equals(datetime.truncatedTo(ChronoUnit.DAYS)))
                .collect(Collectors.toList());
        BigDecimal high = todayMinuteOhlcvs.stream()
                .map(Ohlcv::getHigh)
                .reduce(BigDecimal::max)
                .orElseThrow();
        BigDecimal low = todayMinuteOhlcvs.stream()
                .map(Ohlcv::getLow)
                .reduce(BigDecimal::min)
                .orElseThrow();
        BigDecimal closePrice = todayMinuteOhlcvs.get(0).getClose();
        BigDecimal volume = todayMinuteOhlcvs.stream()
                .map(Ohlcv::getVolume)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Ohlcv todayDailyOhlcv = dailyOhlcvs.get(0);
        todayDailyOhlcv.setHigh(high);
        todayDailyOhlcv.setLow(low);
        todayDailyOhlcv.setClose(closePrice);
        todayDailyOhlcv.setVolume(volume);

        // return
        return dailyOhlcvs;
    }

    @Override
    public BigDecimal getTickPrice(Asset asset, BigDecimal price) throws InterruptedException {
        return null;
    }

    @Override
    public boolean isOverMinimumOrderAmount(BigDecimal quantity, BigDecimal price) throws InterruptedException {
        return quantity.compareTo(BigDecimal.ONE) >= 0;
    }

    @Override
    public OrderBook getOrderBook(Asset asset) {
        loadOhlcvsIfNotExist(asset, datetime);
        LocalDateTime datetimeTo = datetime.truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime datetimeFrom = datetimeTo.minus(1, ChronoUnit.MINUTES);
        List<Ohlcv> minuteOhlcvs = minuteOhlcvsMap.get(asset.getAssetId());
        Ohlcv minuteOhlcv = minuteOhlcvs.stream()
                .filter(assetOhlcv -> (assetOhlcv.getDateTime().isAfter(datetimeFrom) || assetOhlcv.getDateTime().equals(datetimeFrom))
                        && (assetOhlcv.getDateTime().isBefore(datetimeTo) || assetOhlcv.getDateTime().equals(datetimeTo)))
                .findFirst()
                .orElse(null);
        BigDecimal price = minuteOhlcv.getClose();
        BigDecimal bidPrice = minuteOhlcv.getClose();
        BigDecimal askPrice = minuteOhlcv.getClose();
        return OrderBook.builder()
                .price(price)
                .bidPrice(bidPrice)
                .askPrice(askPrice)
                .build();
    }

    @Override
    public Balance getBalance() {
        BigDecimal balanceValuationAmount = BigDecimal.ZERO;
        for (BalanceAsset balanceAsset : balanceAssets) {
            OrderBook orderBook = getOrderBook(balanceAsset);
            BigDecimal assetCurrentPrice = orderBook.getPrice();

            BigDecimal assetQuantity = balanceAsset.getQuantity();
            BigDecimal assetValuationAmount = assetQuantity.multiply(assetCurrentPrice, MathContext.DECIMAL32);
            BigDecimal assetProfitAmount = assetValuationAmount.subtract(balanceAsset.getPurchaseAmount());

            // updates balance asset
            balanceAsset.setValuationAmount(assetValuationAmount);
            balanceAsset.setProfitAmount(assetProfitAmount);

            // add balance valuation amount
            balanceValuationAmount = balanceValuationAmount.add(assetValuationAmount);
        }
        BigDecimal totalAmount = balanceCashAmount.add(balanceValuationAmount);

        // return
        return Balance.builder()
                .totalAmount(totalAmount)
                .cashAmount(balanceCashAmount)
                .valuationAmount(balanceValuationAmount)
                .balanceAssets(balanceAssets)
                .build();
    }

    @Override
    public Order submitOrder(Asset asset, Order order) throws InterruptedException {
        order.setOrderAt(datetime.atZone(timezone).toInstant());

        // order book
        OrderBook orderBook = getOrderBook(asset);

        // balance asset
        BalanceAsset balanceAsset = balanceAssets.stream()
                .filter(it -> Objects.equals(it.getAssetId(),  order.getAssetId()))
                .findFirst()
                .orElse(null);

        // buy
        if(order.getType() == Order.Type.BUY) {
            BigDecimal buyQuantity = order.getQuantity()
                    .setScale(0, RoundingMode.FLOOR);
            order.setQuantity(buyQuantity);

            // buy price, amount
            BigDecimal buyPrice = orderBook.getAskPrice();
            BigDecimal buyAmount = buyQuantity.multiply(buyPrice, MathContext.DECIMAL32);
            if(balanceAsset == null) {
                balanceAsset = BalanceAsset.builder()
                        .assetId(order.getAssetId())
                        .assetName(order.getAssetName())
                        .quantity(buyQuantity)
                        .orderableQuantity(buyQuantity)
                        .purchasePrice(buyPrice)
                        .purchaseAmount(buyAmount)
                        .build();
                balanceAssets.add(balanceAsset);
            }else{
                BigDecimal quantity = balanceAsset.getQuantity().add(buyQuantity);
                BigDecimal purchaseAmount = balanceAsset.getPurchaseAmount().add(buyAmount);
                BigDecimal purchasePrice = purchaseAmount.divide(quantity, MathContext.DECIMAL32);
                balanceAsset.setQuantity(quantity);
                balanceAsset.setOrderableQuantity(quantity);
                balanceAsset.setPurchaseAmount(purchaseAmount);
                balanceAsset.setPurchasePrice(purchasePrice);
            }

            // withdraw
            withdraw(buyAmount);

            // deduct fee
            deductFee(buyAmount);
        }

        // sell
        if(order.getType() == Order.Type.SELL) {
            Objects.requireNonNull(balanceAsset, "balance asset is null");
            BigDecimal sellQuantity = order.getQuantity()
                    .setScale(0, RoundingMode.FLOOR);
            order.setQuantity(sellQuantity);

            // sell price, amount
            BigDecimal sellPrice = orderBook.getBidPrice();
            BigDecimal sellAmount = sellQuantity.multiply(sellPrice, MathContext.DECIMAL32);

            // realized profit amount
            BigDecimal realizedProfitAmount = sellPrice
                    .subtract(balanceAsset.getPurchasePrice())
                    .multiply(sellQuantity, MathContext.DECIMAL32);
            simulateReport.addAssetReturn(balanceAsset, datetime, realizedProfitAmount);

            // updates balance asset
            BigDecimal remainedQuantity = balanceAsset.getQuantity().subtract(sellQuantity);
            if(remainedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                balanceAssets.removeIf(it -> Objects.equals(it.getAssetId(), order.getAssetId()));
            }else{
                BigDecimal purchaseAmount = balanceAsset.getPurchaseAmount().subtract(sellAmount);
                BigDecimal purchasePrice = purchaseAmount.divide(remainedQuantity, MathContext.DECIMAL32);
                balanceAsset.setQuantity(remainedQuantity);
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

        // snapshot simulate report
        snapshotSimulateReport();

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

    void snapshotSimulateReport() {
        Balance balance = getBalance();
        simulateReport.addTotalReturn(datetime, balance.getTotalAmount());
    }

}
