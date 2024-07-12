package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Setter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;

@Builder
public class TradeExecutor {

    private final PlatformTransactionManager transactionManager;

    private final BasketService basketService;

    private final AssetService assetService;

    private final OhlcvService ohlcvService;

    private final NewsService newsService;

    private final OrderService orderService;

    private final AlarmService alarmService;

    @Setter
    private Logger log;

    private final Map<String, StrategyResult> strategyResultMap = new HashMap<>();

    private final Map<String, Integer> strategyResultValueMatchCountMap = new HashMap<>();

    @Setter
    private TradeAssetStore tradeAssetStore;

    /**
     * executes trade
     * @param trade trade info
     * @param strategy strategy info
     * @param dateTime date time
     * @param brokerClient broker client
     */
    public void execute(Trade trade, Strategy strategy, LocalDateTime dateTime, BrokerClient brokerClient) throws InterruptedException {
        log.info("=".repeat(80));
        log.info("[{}] execute trade", trade.getTradeName());

        // time zone
        ZoneId timeZone = brokerClient.getDefinition().getTimezone();
        log.info("[{}] market timeZone: {}", trade.getTradeName(), timeZone);
        log.info("[{}] market dateTime: {}", trade.getTradeName(), dateTime);

        // checks start,end time
        if (!isOperatingTime(trade, dateTime)) {
            log.info("[{}] not operating time - {} ~ {}", trade.getTradeName(), trade.getStartTime(), trade.getEndTime());
            return;
        }

        // check market opened
        if(!brokerClient.isOpened(dateTime)) {
            log.info("[{}] market not opened.", trade.getTradeName());
            return;
        }

        // basket
        Basket basket = basketService.getBasket(trade.getBasketId()).orElseThrow();
        log.info("[{}] basket: {}", trade.getTradeName(), basket.getBasketName());

        // balance
        Balance balance = brokerClient.getBalance();

        // checks buy condition
        for (BasketAsset basketAsset : basket.getBasketAssets()) {
            try {
                Thread.sleep(100);

                // check enabled
                if (!basketAsset.isEnabled()) {
                    continue;
                }

                // logging
                log.info("-".repeat(80));
                log.info("[{} - {}] check asset", basketAsset.getAssetId(), basketAsset.getAssetName());

                // daily ohlcvs
                List<Ohlcv> dailyOhlcvs = brokerClient.getDailyOhlcvs(basketAsset);
                List<Ohlcv> previousDailyOhlcvs = getPreviousDailyOhlcvs(basketAsset.getAssetId(), dailyOhlcvs, dateTime);
                dailyOhlcvs.addAll(previousDailyOhlcvs);

                // minute ohlcvs
                List<Ohlcv> minuteOhlcvs = brokerClient.getMinuteOhlcvs(basketAsset);
                List<Ohlcv> previousMinuteOhlcvs = getPreviousMinuteOhlcvs(basketAsset.getAssetId(), minuteOhlcvs, dateTime);
                minuteOhlcvs.addAll(previousMinuteOhlcvs);

                // newses
                List<News> newses = assetService.getNewses(
                        basketAsset.getAssetId(),
                        dateTime.minusWeeks(1),
                        dateTime,
                        Pageable.unpaged());

                // creates trade asset
                TradeAsset tradeAsset = tradeAssetStore.load(trade.getTradeId(), basketAsset.getAssetId())
                        .orElse(TradeAsset.builder()
                                .tradeId(trade.getTradeId())
                                .assetId(basketAsset.getAssetId())
                                .build());
                tradeAsset.setAssetName(basketAsset.getAssetName());
                tradeAsset.setAssetName(basketAsset.getAssetName());
                tradeAsset.setMarket(basketAsset.getMarket());
                tradeAsset.setType(basketAsset.getType());
                tradeAsset.setExchange(basketAsset.getExchange());
                tradeAsset.setMarketCap(basketAsset.getMarketCap());
                tradeAsset.setPreviousClose(dailyOhlcvs.get(1).getClose());
                tradeAsset.setOpen(dailyOhlcvs.get(0).getOpen());
                tradeAsset.setClose(minuteOhlcvs.get(0).getClose());
                tradeAsset.setDailyOhlcvs(dailyOhlcvs);
                tradeAsset.setMinuteOhlcvs(minuteOhlcvs);
                tradeAsset.setNewses(newses);

                // logging
                log.info("[{} - {}] dailyOhlcvs({}):{}", tradeAsset.getAssetId(), tradeAsset.getAssetName(), tradeAsset.getDailyOhlcvs().size(), tradeAsset.getDailyOhlcvs().isEmpty() ? null : tradeAsset.getDailyOhlcvs().get(0));
                log.info("[{} - {}] minuteOhlcvs({}):{}", tradeAsset.getAssetId(), tradeAsset.getAssetName(), tradeAsset.getMinuteOhlcvs().size(), tradeAsset.getMinuteOhlcvs().isEmpty() ? null : tradeAsset.getMinuteOhlcvs().get(0));

                // order book
                OrderBook orderBook = brokerClient.getOrderBook(basketAsset);

                // balance asset
                BalanceAsset balanceAsset = balance.getBalanceAsset(basketAsset.getAssetId()).orElse(null);

                // executes trade asset decider
                StrategyExecutor strategyExecutor = StrategyExecutor.builder()
                        .strategy(strategy)
                        .variables(trade.getStrategyVariables())
                        .dateTime(dateTime)
                        .tradeAsset(tradeAsset)
                        .orderBook(orderBook)
                        .balance(balance)
                        .balanceAsset(balanceAsset)
                        .build();
                strategyExecutor.setLog(log);
                Instant startTime = Instant.now();
                StrategyResult strategyResult = strategyExecutor.execute();
                log.info("[{} - {}] strategy execution elapsed:{}", basketAsset.getAssetId(), basketAsset.getAssetName(), Duration.between(startTime, Instant.now()));
                log.info("[{} - {}] strategy result: {}", basketAsset.getAssetId(), basketAsset.getAssetName(), strategyResult);

                // save trade asset to store
                if (tradeAssetStore != null) {
                    tradeAssetStore.save(tradeAsset);
                }

                // check strategy result and count
                StrategyResult previousStrategyResult = strategyResultMap.get(basketAsset.getAssetId());
                int strategyResultValueMatchCount = strategyResultValueMatchCountMap.getOrDefault(basketAsset.getAssetId(), 0);
                if (Objects.equals(strategyResult, previousStrategyResult)) {
                    strategyResultValueMatchCount ++;
                } else {
                    strategyResultValueMatchCount = 1;
                }
                strategyResultMap.put(basketAsset.getAssetId(), strategyResult);
                strategyResultValueMatchCountMap.put(basketAsset.getAssetId(), strategyResultValueMatchCount);

                // checks threshold exceeded
                log.info("[{} - {}] strategyResultValueMatchCount: {}", basketAsset.getAssetId(), basketAsset.getAssetName(), strategyResultValueMatchCount);
                if (strategyResultValueMatchCount < trade.getThreshold()) {
                    log.info("[{} - {}] threshold has not been exceeded yet - threshold is {}", basketAsset.getAssetId(), basketAsset.getAssetName(), trade.getThreshold());
                    continue;
                }

                //===============================================
                // 0. holding weight is zero
                //===============================================
                if (basketAsset.getHoldingWeight().compareTo(BigDecimal.ZERO) == 0) {
                    if (balanceAsset != null) {
                        BigDecimal price = calculateSellPrice(tradeAsset, orderBook, brokerClient);
                        BigDecimal quantity = balanceAsset.getOrderableQuantity();
                        sellTradeAsset(brokerClient, trade, tradeAsset, quantity, price, strategyResult, balanceAsset);
                    }
                    continue;
                }

                //===============================================
                // 1. null is no operation
                //===============================================
                if (strategyResult == null) {
                    continue;
                }

                //===============================================
                // 2. apply holding weight
                //===============================================
                // defines
                BigDecimal investAmount = trade.getInvestAmount();
                BigDecimal holdingWeight = basketAsset.getHoldingWeight();
                BigDecimal holdingWeightAmount = investAmount
                        .divide(BigDecimal.valueOf(100), MathContext.DECIMAL32)
                        .multiply(holdingWeight)
                        .setScale(2, RoundingMode.HALF_UP);

                StrategyResult.Action action = strategyResult.getAction();
                BigDecimal position = strategyResult.getPosition();
                BigDecimal positionAmount = holdingWeightAmount
                        .multiply(position)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal currentOwnedAmount = balance.getBalanceAsset(basketAsset.getAssetId())
                        .map(BalanceAsset::getValuationAmount)
                        .orElse(BigDecimal.ZERO);

                // buy
                if (action == StrategyResult.Action.BUY) {
                    BigDecimal buyAmount = positionAmount.subtract(currentOwnedAmount);
                    if (buyAmount.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal buyPrice = calculateBuyPrice(tradeAsset, orderBook, brokerClient);
                        BigDecimal buyQuantity = buyAmount
                                .divide(buyPrice, MathContext.DECIMAL32)
                                .setScale(0, RoundingMode.HALF_UP);
                        // check minimum order amount
                        boolean canBuy = brokerClient.isOverMinimumOrderAmount(buyQuantity, buyPrice);
                        if (canBuy) {
                            buyTradeAsset(brokerClient, trade, tradeAsset, buyQuantity, buyPrice, strategyResult);
                        }
                    }
                    continue;
                }

                // sell
                if (action == StrategyResult.Action.SELL) {
                    BigDecimal sellAmount = currentOwnedAmount.subtract(positionAmount);
                    if (sellAmount.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal sellPrice = calculateSellPrice(tradeAsset, orderBook, brokerClient);
                        BigDecimal sellQuantity = sellAmount
                                .divide(sellPrice, MathContext.DECIMAL32)
                                .setScale(0, RoundingMode.HALF_UP);
                        // check minimum order amount
                        boolean canSell = brokerClient.isOverMinimumOrderAmount(sellQuantity, sellPrice);
                        if (canSell) {
                            sellTradeAsset(brokerClient, trade, tradeAsset, sellQuantity, sellPrice, strategyResult, balanceAsset);
                        }
                    }
                    continue;
                }

            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                sendErrorAlarmIfEnabled(trade, basketAsset, e);
            }
        }
    }

    /**
     * checks operating time
     * @param trade trade info
     * @param dateTime date time
     * @return whether date time is operable
     */
    private boolean isOperatingTime(Trade trade, LocalDateTime dateTime) {
        if(trade.getStartTime() == null || trade.getEndTime() == null) {
            return false;
        }
        LocalTime startTime = trade.getStartTime();
        LocalTime endTime = trade.getEndTime();
        LocalTime currentTime = dateTime.toLocalTime();
        if (startTime.isAfter(endTime)) {
            return !(currentTime.isBefore(startTime) || currentTime.equals(startTime))
                    || !(currentTime.isAfter(endTime) || currentTime.equals(endTime));
        } else {
            return (currentTime.isAfter(startTime) || currentTime.equals(startTime))
                    && (currentTime.isBefore(endTime) || currentTime.equals(endTime));
        }
    }

    /**
     * return previous daily ohlcvs
     * @param assetId asset id
     * @param ohlcvs ohlcvs
     * @param dateTime date time
     * @return previous daily ohlcvs
     */
    private List<Ohlcv> getPreviousDailyOhlcvs(String assetId, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusYears(1);
        LocalDateTime dateTimeTo = ohlcvs.isEmpty()
                ? dateTime
                : ohlcvs.get(ohlcvs.size()-1).getDateTime().minusDays(1);
        if(dateTimeTo.isBefore(dateTimeFrom)) {
            return new ArrayList<>();
        }
        return ohlcvService.getDailyOhlcvs(assetId, dateTimeFrom, dateTimeTo, PageRequest.of(0, 360));
    }

    /**
     * return previous minute ohlcvs
     * @param assetId asset id
     * @param ohlcvs ohlcvs
     * @param dateTime date time
     * @return previous minute ohlcvs
     */
    private List<Ohlcv> getPreviousMinuteOhlcvs(String assetId, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusWeeks(1);
        LocalDateTime dateTimeTo = ohlcvs.isEmpty()
                ? dateTime
                : ohlcvs.get(ohlcvs.size()-1).getDateTime().minusMinutes(1);
        if(dateTimeTo.isBefore(dateTimeFrom)) {
            return new ArrayList<>();
        }
        return ohlcvService.getMinuteOhlcvs(assetId, dateTimeFrom, dateTimeTo, PageRequest.of(0, 1000));
    }

    /**
     * calculates buy price
     * @param tradeAsset trade asset
     * @param orderBook order book
     * @param brokerClient broker client
     * @return buy price
     */
    private BigDecimal calculateBuyPrice(TradeAsset tradeAsset, OrderBook orderBook, BrokerClient brokerClient) throws InterruptedException {
        BigDecimal price = orderBook.getAskPrice();
        BigDecimal tickPrice = brokerClient.getTickPrice(tradeAsset, price);
        // max competitive price (매도 호가 에서 1틱 유리한 가격 설정)
        if(tickPrice != null) {
            price = price.subtract(tickPrice);
        }
        return price.max(orderBook.getBidPrice());
    }

    /**
     * calculates sell price
     * @param tradeAsset trade asset
     * @param orderBook order book
     * @param brokerClient broker client
     * @return sell price
     */
    private BigDecimal calculateSellPrice(TradeAsset tradeAsset, OrderBook orderBook, BrokerClient brokerClient) throws InterruptedException {
        BigDecimal price = orderBook.getBidPrice();
        BigDecimal tickPrice = brokerClient.getTickPrice(tradeAsset, price);
        // min competitive price (매수 호가 에서 1틱 유리한 가격 설정)
        if(tickPrice != null) {
            price = price.add(tickPrice);
        }
        return price.min(orderBook.getAskPrice());
    }

    /**
     * buys trade asset
     * @param brokerClient broker client
     * @param trade trade
     * @param tradeAsset trade asset
     * @param quantity buy quantity
     * @param price buy price for unit
     * @param strategyResult strategy result
     */
    private void buyTradeAsset(BrokerClient brokerClient, Trade trade, TradeAsset tradeAsset, BigDecimal quantity, BigDecimal price, StrategyResult strategyResult) throws InterruptedException {
        Order order = Order.builder()
                .orderAt(Instant.now())
                .type(Order.Type.BUY)
                .kind(trade.getOrderKind())
                .tradeId(trade.getTradeId())
                .assetId(tradeAsset.getAssetId())
                .assetName(tradeAsset.getAssetName())
                .quantity(quantity)
                .price(price)
                .strategyResult(strategyResult)
                .build();
        log.info("[{}] buyTradeAsset: {}", tradeAsset.getAssetName(), order);
        try {
            // check waiting order exists
            Order waitingOrder = brokerClient.getWaitingOrders().stream()
                    .filter(element ->
                            Objects.equals(element.getSymbol(), order.getSymbol())
                                    && element.getType() == order.getType())
                    .findFirst()
                    .orElse(null);
            if (waitingOrder != null) {
                // if limit type order, amend order
                if (waitingOrder.getKind() == Order.Kind.LIMIT) {
                    waitingOrder.setPrice(price);
                    log.info("[{}] amend buy order:{}", tradeAsset.getAssetName(), waitingOrder);
                    brokerClient.amendOrder(tradeAsset, waitingOrder);
                }
                return;
            }

            // submit buy order
            brokerClient.submitOrder(tradeAsset, order);
            order.setResult(Order.Result.COMPLETED);

            // alarm
            sendOrderAlarmIfEnabled(trade, order);

        } catch (Throwable e) {
            order.setResult(Order.Result.FAILED);
            order.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            saveTradeOrder(order);
        }
    }

    /**
     * sells trade asset
     * @param brokerClient broker client
     * @param trade trade
     * @param tradeAsset basket asset
     * @param quantity sell quantity
     * @param price sell price
     * @param strategyResult strategy result
     * @param balanceAsset balance asset
     */
    private void sellTradeAsset(BrokerClient brokerClient, Trade trade, TradeAsset tradeAsset, BigDecimal quantity, BigDecimal price, StrategyResult strategyResult, BalanceAsset balanceAsset) throws InterruptedException {
        Order order = Order.builder()
                .orderAt(Instant.now())
                .type(Order.Type.SELL)
                .kind(trade.getOrderKind())
                .tradeId(trade.getTradeId())
                .assetId(tradeAsset.getAssetId())
                .assetName(tradeAsset.getAssetName())
                .quantity(quantity)
                .price(price)
                .strategyResult(strategyResult)
                .build();
        log.info("[{}] sellTradeAsset: {}", tradeAsset.getAssetName(), order);

        // purchase price, realized amount
        if (balanceAsset.getPurchasePrice() != null) {
            order.setPurchasePrice(balanceAsset.getPurchasePrice());
            BigDecimal realizedProfitAmount = price.subtract(balanceAsset.getPurchasePrice())
                    .multiply(quantity)
                    .setScale(4, RoundingMode.FLOOR);
            order.setRealizedProfitAmount(realizedProfitAmount);
        }

        try {
            // check waiting order exists
            Order waitingOrder = brokerClient.getWaitingOrders().stream()
                    .filter(element ->
                            Objects.equals(element.getSymbol(), order.getSymbol())
                                    && element.getType() == order.getType())
                    .findFirst()
                    .orElse(null);
            if (waitingOrder != null) {
                // if limit type order, amend order
                if (waitingOrder.getKind() == Order.Kind.LIMIT) {
                    waitingOrder.setPrice(price);
                    log.info("[{}] amend sell order:{}", tradeAsset.getAssetName(), waitingOrder);
                    brokerClient.amendOrder(tradeAsset, waitingOrder);
                }
                return;
            }

            // submit sell order
            brokerClient.submitOrder(tradeAsset, order);
            order.setResult(Order.Result.COMPLETED);

            // alarm
            sendOrderAlarmIfEnabled(trade, order);

        } catch (Throwable e) {
            order.setResult(Order.Result.FAILED);
            order.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            saveTradeOrder(order);
        }
    }

    /**
     * saves trade order
     * @param order order info
     */
    private void saveTradeOrder(Order order) {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager, transactionDefinition);
        transactionTemplate.executeWithoutResult(transactionStatus ->
                orderService.saveOrder(order));
    }

    /**
     * send error alarm if enable
     * @param trade trade
     * @param asset basket asset
     * @param t throwable
     */
    private void sendErrorAlarmIfEnabled(Trade trade, Asset asset, Throwable t) {
        if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
            if (trade.isAlarmOnError()) {
                String subject = String.format("[%s - %s] Error", trade.getTradeName(), asset != null ? asset.getAssetName() : "");
                String content = ExceptionUtils.getRootCause(t).getMessage();
                alarmService.sendAlarm(trade.getAlarmId(), subject, content);
            }
        }
    }

    /**
     * send order alarm if enable
     * @param trade trade
     * @param order order
     */
    private void sendOrderAlarmIfEnabled(Trade trade, Order order) {
        if (trade.isAlarmOnOrder()) {
            if (trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                // subject
                StringBuilder subject = new StringBuilder();
                subject.append(String.format("[%s - %s] %s", trade.getTradeName(), order.getAssetName(), order.getType()));
                // content
                StringBuilder content = new StringBuilder();
                content.append(String.format("- kind: %s", order.getKind())).append('\n');
                content.append(String.format("- price: %s", order.getPrice())).append('\n');
                content.append(String.format("- quantity: %s", order.getQuantity())).append('\n');
                content.append(String.format("- strategyResult: %s", order.getStrategyResult())).append('\n');
                alarmService.sendAlarm(trade.getAlarmId(), subject.toString(), content.toString());
            }
        }
    }

}
