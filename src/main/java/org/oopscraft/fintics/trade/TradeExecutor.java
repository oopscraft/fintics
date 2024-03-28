package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.dao.AssetOhlcvRepository;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.dao.OrderEntity;
import org.oopscraft.fintics.dao.OrderRepository;
import org.oopscraft.fintics.model.*;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class TradeExecutor {

    private final PlatformTransactionManager transactionManager;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    private final AssetOhlcvRepository assetOhlcvRepository;

    private final OrderRepository orderRepository;

    private final AlarmService alarmService;

    private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    private final Map<String,BigDecimal> ruleScriptResultMap = new HashMap<>();

    private final Map<String,Integer> ruleScriptResultCountMap = new HashMap<>();

    @Builder
    private TradeExecutor(PlatformTransactionManager transactionManager, IndiceOhlcvRepository indiceOhlcvRepository, AssetOhlcvRepository assetOhlcvRepository, OrderRepository orderRepository, AlarmService alarmService) {
        this.transactionManager = transactionManager;
        this.indiceOhlcvRepository = indiceOhlcvRepository;
        this.assetOhlcvRepository = assetOhlcvRepository;
        this.orderRepository = orderRepository;
        this.alarmService = alarmService;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public void execute(Trade trade, LocalDateTime dateTime, IndiceClient indiceClient, TradeClient tradeClient) throws InterruptedException {
        log.info("[{}] Check trade", trade.getTradeName());

        // check market opened
        if(!tradeClient.isOpened(dateTime)) {
            log.info("[{}] Market not opened.", trade.getTradeName());
            return;
        }

        // checks start,end time
        if (!isOperatingTime(trade, dateTime)) {
            log.info("[{}] Not operating time - {} ~ {}", trade.getTradeName(), trade.getStartAt(), trade.getEndAt());
            return;
        }

        // indice indicators
        List<IndiceIndicator> indiceIndicators = new ArrayList<>();
        for(IndiceId indiceId : IndiceId.values()) {
            // minute ohlcvs
            List<Ohlcv> minuteOhlcvs = indiceClient.getMinuteOhlcvs(indiceId, dateTime);
            List<Ohlcv> previousMinuteOhlcvs = getPreviousIndiceMinuteOhlcvs(indiceId, minuteOhlcvs, dateTime);
            minuteOhlcvs.addAll(previousMinuteOhlcvs);

            // daily ohlcvs
            List<Ohlcv> dailyOhlcvs = indiceClient.getDailyOhlcvs(indiceId, dateTime);
            List<Ohlcv> previousDailyOhlcvs = getPreviousIndiceDailyOhlcvs(indiceId, dailyOhlcvs, dateTime);
            dailyOhlcvs.addAll(previousDailyOhlcvs);

            // add indicator
            indiceIndicators.add(IndiceIndicator.builder()
                    .indiceId(indiceId)
                    .minuteOhlcvs(minuteOhlcvs)
                    .dailyOhlcvs(dailyOhlcvs)
                    .build());
        }

        // balance
        Balance balance = tradeClient.getBalance();

        // checks buy condition
        for (TradeAsset tradeAsset : trade.getTradeAssets()) {
            try {
                Thread.sleep(100);

                // check enabled
                if (!tradeAsset.isEnabled()) {
                    continue;
                }

                // logging
                log.info("[{}] Check asset", tradeAsset.getAssetName());

                // indicator
                List<Ohlcv> minuteOhlcvs = tradeClient.getMinuteOhlcvs(tradeAsset, dateTime);
                List<Ohlcv> previousMinuteOhlcvs = getPreviousAssetMinuteOhlcvs(trade.getTradeClientId(), tradeAsset.getAssetId(), minuteOhlcvs, dateTime);
                minuteOhlcvs.addAll(previousMinuteOhlcvs);

                List<Ohlcv> dailyOhlcvs = tradeClient.getDailyOhlcvs(tradeAsset, dateTime);
                List<Ohlcv> previousDailyOhlcvs = getPreviousAssetDailyOhlcvs(trade.getTradeClientId(), tradeAsset.getAssetId(), dailyOhlcvs, dateTime);
                dailyOhlcvs.addAll(previousDailyOhlcvs);

                AssetIndicator assetIndicator = AssetIndicator.builder()
                        .assetId(tradeAsset.getAssetId())
                        .assetName(tradeAsset.getAssetName())
                        .minuteOhlcvs(minuteOhlcvs)
                        .dailyOhlcvs(dailyOhlcvs)
                        .build();
                log.info("[{}] MinuteOhlcvs({}):{}", tradeAsset.getAssetName(), assetIndicator.getMinuteOhlcvs().size(), assetIndicator.getMinuteOhlcvs().isEmpty() ? null : assetIndicator.getMinuteOhlcvs().get(0));
                log.info("[{}] DailyOhlcvs({}):{}", tradeAsset.getAssetName(), assetIndicator.getDailyOhlcvs().size(), assetIndicator.getDailyOhlcvs().isEmpty() ? null : assetIndicator.getDailyOhlcvs().get(0));

                // order book
                OrderBook orderBook = tradeClient.getOrderBook(tradeAsset);

                // executes trade asset decider
                RuleScriptExecutor ruleScriptExecutor = RuleScriptExecutor.builder()
                        .ruleConfig(trade.getRuleConfig())
                        .ruleScript(trade.getRuleScript())
                        .dateTime(dateTime)
                        .orderBook(orderBook)
                        .balance(balance)
                        .indiceIndicators(indiceIndicators)
                        .assetIndicator(assetIndicator)
                        .build();
                ruleScriptExecutor.setLog(log);
                Instant startTime = Instant.now();
                BigDecimal ruleScriptResult = ruleScriptExecutor.execute();
                log.info("[{}] Rule script execution elapsed:{}", tradeAsset.getAssetName(), Duration.between(startTime, Instant.now()));
                log.info("[{}] ruleScriptResult: {}", tradeAsset.getAssetName(), ruleScriptResult);

                // check rule script result and count
                BigDecimal previousRuleScriptResult = ruleScriptResultMap.get(tradeAsset.getAssetId());
                int ruleScriptResultCount = ruleScriptResultCountMap.getOrDefault(tradeAsset.getAssetId(), 0);
                if (Objects.equals(ruleScriptResult, previousRuleScriptResult)) {
                    ruleScriptResultCount++;
                } else {
                    ruleScriptResultCount = 1;
                }
                ruleScriptResultMap.put(tradeAsset.getAssetId(), ruleScriptResult);
                ruleScriptResultCountMap.put(tradeAsset.getAssetId(), ruleScriptResultCount);

                // checks threshold exceeded
                log.info("[{}] ruleScriptResultCount: {}", tradeAsset.getAssetName(), ruleScriptResultCount);
                if (ruleScriptResultCount < trade.getThreshold()) {
                    log.info("[{}] Threshold has not been exceeded yet - threshold is {}", tradeAsset.getAssetName(), trade.getThreshold());
                    continue;
                }

                // null is no operation
                if (ruleScriptResult == null) {
                    continue;
                }

                // calculate exceeded amount
                BigDecimal totalAmount = balance.getTotalAmount();
                BigDecimal holdRatio = tradeAsset.getHoldRatio();
                BigDecimal holdRatioAmount = totalAmount
                        .divide(BigDecimal.valueOf(100), MathContext.DECIMAL32)
                        .multiply(holdRatio)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal holdConditionResultAmount = holdRatioAmount
                        .multiply(ruleScriptResult)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal balanceAssetAmount = balance.getBalanceAsset(tradeAsset.getAssetId())
                        .map(BalanceAsset::getValuationAmount)
                        .orElse(BigDecimal.ZERO);
                BigDecimal exceededAmount = holdConditionResultAmount.subtract(balanceAssetAmount);

                // check change is over 10%(9%)
                BigDecimal thresholdAmount = holdRatioAmount.multiply(BigDecimal.valueOf(0.09));
                if(exceededAmount.abs().compareTo(thresholdAmount) < 0) {
                    continue;
                }

                // buy (exceedAmount is over zero)
                if (exceededAmount.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal price = orderBook.getAskPrice();
                    BigDecimal priceTick = tradeClient.getPriceTick(tradeAsset, price);
                    if(priceTick != null) {
                        price = price.min(orderBook.getBidPrice().add(priceTick));
                    }
                    BigDecimal quantity = exceededAmount.divide(price, MathContext.DECIMAL32);
                    buyTradeAsset(tradeClient, trade, tradeAsset, quantity, price);
                }

                // sell (exceedAmount is under zero)
                if (exceededAmount.compareTo(BigDecimal.ZERO) < 0) {
                    BigDecimal price = orderBook.getBidPrice();
                    BigDecimal priceTick = tradeClient.getPriceTick(tradeAsset, price);
                    if(priceTick != null) {
                        price = price.max(orderBook.getAskPrice().subtract(priceTick));
                    }
                    BigDecimal quantity = exceededAmount.abs().divide(price, MathContext.DECIMAL32);
                    // if holdConditionResult is zero, sell quantity is all
                    if (ruleScriptResult.compareTo(BigDecimal.ZERO) == 0) {
                        BalanceAsset balanceAsset = balance.getBalanceAsset(tradeAsset.getAssetId()).orElse(null);
                        if (balanceAsset != null) {
                            quantity = balanceAsset.getOrderableQuantity();
                        }
                    }
                    sellTradeAsset(tradeClient, trade, tradeAsset, quantity, price);
                }

            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                sendErrorAlarmIfEnabled(trade, tradeAsset, e);
            }
        }
    }

    private boolean isOperatingTime(Trade trade, LocalDateTime dateTime) {
        if(trade.getStartAt() == null || trade.getEndAt() == null) {
            return false;
        }
        LocalTime time = dateTime.toLocalTime();
        return time.isAfter(trade.getStartAt()) && time.isBefore(trade.getEndAt());
    }

    private List<Ohlcv> getPreviousIndiceMinuteOhlcvs(IndiceId indiceId, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusWeeks(1);
        LocalDateTime dateTimeTo = ohlcvs.isEmpty() ? dateTime : ohlcvs.get(ohlcvs.size()-1).getDateTime().minusMinutes(1);
        if(dateTimeTo.isBefore(dateTimeFrom)) {
            return new ArrayList<>();
        }
        return indiceOhlcvRepository.findAllByIndiceIdAndType(
                        indiceId,
                        Ohlcv.Type.MINUTE,
                        dateTimeFrom,
                        dateTimeTo,
                        PageRequest.of(0, 1000)
                ).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private List<Ohlcv> getPreviousIndiceDailyOhlcvs(IndiceId indiceId, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusYears(1);
        LocalDateTime dateTimeTo = ohlcvs.isEmpty() ? dateTime : ohlcvs.get(ohlcvs.size()-1).getDateTime().minusDays(1);
        if(dateTimeTo.isBefore(dateTimeFrom)) {
            return new ArrayList<>();
        }
        return indiceOhlcvRepository.findAllByIndiceIdAndType(
                        indiceId,
                        Ohlcv.Type.MINUTE,
                        dateTimeFrom,
                        dateTimeTo,
                        PageRequest.of(0, 360)
                )
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private List<Ohlcv> getPreviousAssetMinuteOhlcvs(String tradeClientId, String assetId, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusWeeks(1);
        LocalDateTime dateTimeTo = ohlcvs.isEmpty() ? dateTime : ohlcvs.get(ohlcvs.size()-1).getDateTime().minusMinutes(1);
        if(dateTimeTo.isBefore(dateTimeFrom)) {
            return new ArrayList<>();
        }
        return assetOhlcvRepository.findAllByAssetIdAndType(
                        assetId,
                        Ohlcv.Type.MINUTE,
                        dateTimeFrom,
                        dateTimeTo,
                        PageRequest.of(0, 1000))
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private List<Ohlcv> getPreviousAssetDailyOhlcvs(String tradeClientId, String assetId, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusYears(1);
        LocalDateTime dateTimeTo = ohlcvs.isEmpty() ? dateTime : ohlcvs.get(ohlcvs.size()-1).getDateTime().minusDays(1);
        if(dateTimeTo.isBefore(dateTimeFrom)) {
            return new ArrayList<>();
        }
        return assetOhlcvRepository.findAllByAssetIdAndType(
                        assetId,
                        Ohlcv.Type.MINUTE,
                        dateTimeFrom,
                        dateTimeTo,
                        PageRequest.of(0, 360)
                ).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private void sendErrorAlarmIfEnabled(Trade trade, TradeAsset tradeAsset, Throwable t) {
        if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
            if (trade.isAlarmOnError()) {
                String subject = String.format("[%s - %s]", trade.getTradeName(), tradeAsset != null ? tradeAsset.getAssetName() : "");
                String content = ExceptionUtils.getRootCause(t).getMessage();
                alarmService.sendAlarm(trade.getAlarmId(), subject, content);
            }
        }
    }

    private void buyTradeAsset(TradeClient tradeClient, Trade trade, TradeAsset tradeAsset, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        Order order = Order.builder()
                .orderId(IdGenerator.uuid())
                .orderAt(LocalDateTime.now())
                .type(Order.Type.BUY)
                .kind(trade.getOrderKind())
                .tradeId(tradeAsset.getTradeId())
                .assetId(tradeAsset.getAssetId())
                .assetName(tradeAsset.getAssetName())
                .quantity(quantity)
                .price(price)
                .build();
        log.info("[{}] buyTradeAsset: {}", tradeAsset.getAssetName(), order);
        try {
            // check waiting order exists
            Order waitingOrder = tradeClient.getWaitingOrders().stream()
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
                    tradeClient.amendOrder(waitingOrder);
                }
                return;
            }

            // submit buy order
            tradeClient.submitOrder(order);
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

    private void sellTradeAsset(TradeClient tradeClient, Trade trade, TradeAsset tradeAsset, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        Order order = Order.builder()
                .orderId(IdGenerator.uuid())
                .orderAt(LocalDateTime.now())
                .type(Order.Type.SELL)
                .kind(trade.getOrderKind())
                .tradeId(tradeAsset.getTradeId())
                .assetId(tradeAsset.getAssetId())
                .assetName(tradeAsset.getAssetName())
                .quantity(quantity)
                .price(price)
                .build();
        log.info("[{}] sellTradeAsset: {}", tradeAsset.getAssetName(), order);
        try {
            // check waiting order exists
            Order waitingOrder = tradeClient.getWaitingOrders().stream()
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
                    tradeClient.amendOrder(waitingOrder);
                }
                return;
            }

            // submit sell order
            tradeClient.submitOrder(order);
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

    private void saveTradeOrder(Order order) {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager, transactionDefinition);
        transactionTemplate.executeWithoutResult(transactionStatus ->
                orderRepository.saveAndFlush(OrderEntity.builder()
                        .orderId(order.getOrderId())
                        .orderAt(order.getOrderAt())
                        .type(order.getType())
                        .tradeId(order.getTradeId())
                        .assetId(order.getAssetId())
                        .assetName(order.getAssetName())
                        .kind(order.getKind())
                        .quantity(order.getQuantity())
                        .price(order.getPrice())
                        .result(order.getResult())
                        .errorMessage(order.getErrorMessage())
                        .build()));
    }

    private void sendOrderAlarmIfEnabled(Trade trade, Order order) {
        if (trade.isAlarmOnOrder()) {
            if (trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                String subject = String.format("[%s]", trade.getTradeName());
                String content = String.format("[%s] %s(%s) - price: %s / quantity: %s",
                        order.getAssetName(),
                        order.getType(),
                        order.getKind(),
                        order.getPrice(),
                        order.getQuantity());
                alarmService.sendAlarm(trade.getAlarmId(), subject, content);
            }
        }
    }

}
