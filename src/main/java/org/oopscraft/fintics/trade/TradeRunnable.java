package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class TradeRunnable implements Runnable {

    @Getter
    private final String tradeId;

    @Getter
    private final Integer interval;

    @Getter
    private final TradeLogAppender tradeLogAppender;

    private final PlatformTransactionManager transactionManager;

    private final AlarmService alarmService;

    private final TradeRepository tradeRepository;

    private final IndiceClient indiceClient;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    private final TradeAssetOhlcvRepository tradeAssetOhlcvRepository;

    private final OrderRepository orderRepository;

    private final Logger log;

    @Setter
    @Getter
    private boolean interrupted = false;

    @Builder
    public TradeRunnable(String tradeId, Integer interval, ApplicationContext applicationContext, TradeLogAppender tradeLogAppender) {
        this.tradeId = tradeId;
        this.interval = interval;
        this.tradeLogAppender = tradeLogAppender;

        // component
        this.transactionManager = applicationContext.getBean(PlatformTransactionManager.class);
        this.alarmService = applicationContext.getBean(AlarmService.class);
        this.indiceClient = applicationContext.getBean(IndiceClient.class);
        this.tradeRepository = applicationContext.getBean(TradeRepository.class);
        this.indiceOhlcvRepository = applicationContext.getBean(IndiceOhlcvRepository.class);
        this.tradeAssetOhlcvRepository = applicationContext.getBean(TradeAssetOhlcvRepository.class);
        this.orderRepository = applicationContext.getBean(OrderRepository.class);

        // add log appender
        log = (Logger) LoggerFactory.getLogger(tradeId);
        log.addAppender(this.tradeLogAppender);
    }

    @Override
    public void run() {
        this.tradeLogAppender.start();
        log.info("Start TradeRunnable: {}", tradeId);
        while(!Thread.currentThread().isInterrupted() && !interrupted) {
            TransactionStatus transactionStatus = null;
            try {
                // wait interval
                log.info("Waiting interval: {} seconds", interval);
                Thread.sleep(interval * 1_000);

                // start transaction
                DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
                transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                transactionStatus = transactionManager.getTransaction(transactionDefinition);

                // execute trade
                executeTrade();

                // end transaction
                transactionManager.commit(transactionStatus);

            } catch (InterruptedException e) {
                log.warn("TradeRunnable is interrupted.");
                break;
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            } finally {
                if(transactionStatus != null) {
                    if(!transactionStatus.isCompleted()) {
                        transactionStatus.setRollbackOnly();
                        transactionManager.commit(transactionStatus);
                    }
                }
            }
        }
        log.info("End TradeRunnable: {}", tradeId);
        this.tradeLogAppender.stop();
    }

    private void executeTrade() throws InterruptedException {
        // trade info
        Trade trade = tradeRepository.findById(tradeId)
                .map(Trade::from)
                .orElseThrow();
        try {
            // client
            TradeClient tradeClient = TradeClientFactory.getClient(trade);

            // check market opened
            if(!tradeClient.isOpened()) {
                log.info("Market not opened.");
                return;
            }

            // checks start,end time
            LocalDateTime dateTime = LocalDateTime.now();
            if (!isOperatingTime(trade, dateTime.toLocalTime())) {
                log.info("Not operating time - [{}] {} ~ {}", trade.getName(), trade.getStartAt(), trade.getEndAt());
                return;
            }

            // logging
            log.info("Check trade - [{}]", trade.getName());

            // indice indicators
            List<IndiceIndicator> indiceIndicators = new ArrayList<>();
            for(IndiceSymbol symbol : IndiceSymbol.values()) {
                indiceClient.getMinuteOhlcvs(symbol);

                // minute ohlcvs
                List<Ohlcv> minuteOhlcvs = indiceClient.getMinuteOhlcvs(symbol);
                List<Ohlcv> previousMinuteOhlcvs = getPreviousIndiceMinuteOhlcvs(symbol, minuteOhlcvs);
                minuteOhlcvs.addAll(previousMinuteOhlcvs);

                // daily ohlcvs
                List<Ohlcv> dailyOhlcvs = indiceClient.getDailyOhlcvs(symbol);
                List<Ohlcv> previousDailyOhlcvs = getPreviousIndiceDailyOhlcvs(symbol, dailyOhlcvs);
                dailyOhlcvs.addAll(previousDailyOhlcvs);

                // add indicator
                indiceIndicators.add(IndiceIndicator.builder()
                        .symbol(symbol)
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
                    log.info("Check asset - [{}]", tradeAsset.getName());

                    // indicator
                    List<Ohlcv> minuteOhlcvs = tradeClient.getMinuteOhlcvs(tradeAsset);
                    List<Ohlcv> previousMinuteOhlcvs = getPreviousAssetMinuteOhlcvs(tradeAsset, minuteOhlcvs);
                    minuteOhlcvs.addAll(previousMinuteOhlcvs);

                    List<Ohlcv> dailyOhlcvs = tradeClient.getDailyOhlcvs(tradeAsset);
                    List<Ohlcv> previousDailyOhlcvs = getPreviousAssetDailyOhlcvs(tradeAsset, dailyOhlcvs);
                    dailyOhlcvs.addAll(previousDailyOhlcvs);

                    AssetIndicator assetIndicator = AssetIndicator.builder()
                            .symbol(tradeAsset.getSymbol())
                            .name(tradeAsset.getName())
                            .minuteOhlcvs(minuteOhlcvs)
                            .dailyOhlcvs(dailyOhlcvs)
                            .build();

                    // order book
                    OrderBook orderBook = tradeClient.getOrderBook(tradeAsset);

                    // executes trade asset decider
                    TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                            .holdCondition(trade.getHoldCondition())
                            .logger(log)
                            .dateTime(dateTime)
                            .orderBook(orderBook)
                            .balance(balance)
                            .indiceIndicators(indiceIndicators)
                            .assetIndicator(assetIndicator)
                            .build();
                    Boolean holdConditionResult = tradeAssetDecider.execute();
                    log.info("holdConditionResult: {}", holdConditionResult);

                    // 1. null is no operation
                    if (holdConditionResult == null) {
                        continue;
                    }

                    // 2. buy and hold
                    if (holdConditionResult.equals(Boolean.TRUE)) {
                        if (!balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                            BigDecimal buyAmount = balance.getTotalAmount()
                                    .divide(BigDecimal.valueOf(100), MathContext.DECIMAL32)
                                    .multiply(tradeAsset.getHoldRatio())
                                    .setScale(2, RoundingMode.HALF_UP);
                            BigDecimal price = orderBook.getPrice();
                            BigDecimal quantity = buyAmount
                                    .divide(price, MathContext.DECIMAL32);

                            // buy
                            log.info("Buy asset: {}", tradeAsset.getName());
                            buyTradeAsset(trade, tradeAsset, trade.getOrderType(), quantity, price);
                        }
                    }

                    // 3. sell
                    else if (holdConditionResult.equals(Boolean.FALSE)) {
                        if (balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                            // price, quantity
                            BalanceAsset balanceAsset = balance.getBalanceAsset(tradeAsset.getSymbol()).orElseThrow();
                            BigDecimal price = orderBook.getPrice();
                            BigDecimal quantity = balanceAsset.getOrderableQuantity();

                            // sell
                            log.info("Sell asset: {}", tradeAsset.getName());
                            sellBalanceAsset(trade, balanceAsset, trade.getOrderType(), quantity, price);
                        }
                    }
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                    sendErrorAlarmIfEnabled(trade, tradeAsset, e);
                }
            }
        } catch(InterruptedException e) {
            throw e;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            sendErrorAlarmIfEnabled(trade, null, e);
            throw e;
        }
    }

    private boolean isOperatingTime(Trade trade, LocalTime time) {
        if(trade.getStartAt() == null || trade.getEndAt() == null) {
            return false;
        }
        return time.isAfter(trade.getStartAt()) && time.isBefore(trade.getEndAt());
    }

    private List<Ohlcv> getPreviousIndiceMinuteOhlcvs(IndiceSymbol symbol, List<Ohlcv> ohlcvs) {
        LocalDateTime lastDateTime = !ohlcvs.isEmpty()
                ? ohlcvs.get(ohlcvs.size()-1).getDateTime()
                : LocalDateTime.now();
        return indiceOhlcvRepository.findAllBySymbolAndOhlcvType(symbol,
                        OhlcvType.MINUTE,
                        lastDateTime.minusWeeks(1),
                        lastDateTime.minusMinutes(1),
                        PageRequest.of(0, 360)
                ).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private List<Ohlcv> getPreviousIndiceDailyOhlcvs(IndiceSymbol symbol, List<Ohlcv> ohlcvs) {
        LocalDateTime lastDateTime = !ohlcvs.isEmpty()
                ? ohlcvs.get(ohlcvs.size()-1).getDateTime()
                : LocalDateTime.now();
        return indiceOhlcvRepository.findAllBySymbolAndOhlcvType(
                        symbol,
                        OhlcvType.MINUTE,
                        lastDateTime.minusYears(1),
                        lastDateTime.minusDays(1),
                        PageRequest.of(0, 360)
                )
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private List<Ohlcv> getPreviousAssetMinuteOhlcvs(TradeAsset tradeAsset, List<Ohlcv> ohlcvs) {
        LocalDateTime lastDateTime = !ohlcvs.isEmpty()
                ? ohlcvs.get(ohlcvs.size()-1).getDateTime()
                : LocalDateTime.now();
        return tradeAssetOhlcvRepository.findAllBySymbolAndOhlcvType(
                        tradeAsset.getTradeId(),
                        tradeAsset.getSymbol(),
                        OhlcvType.MINUTE,
                        lastDateTime.minusWeeks(1),
                        lastDateTime.minusMinutes(1),
                        PageRequest.of(0, 1000))
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private List<Ohlcv> getPreviousAssetDailyOhlcvs(TradeAsset tradeAsset, List<Ohlcv> ohlcvs) {
        LocalDateTime lastDateTime = !ohlcvs.isEmpty()
                ? ohlcvs.get(ohlcvs.size()-1).getDateTime()
                : LocalDateTime.now();
        return tradeAssetOhlcvRepository.findAllBySymbolAndOhlcvType(
                        tradeAsset.getTradeId(),
                        tradeAsset.getSymbol(),
                        OhlcvType.MINUTE,
                        lastDateTime.minusYears(1),
                        lastDateTime.minusDays(1),
                        PageRequest.of(0, 360)
                ).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private void sendErrorAlarmIfEnabled(Trade trade, TradeAsset tradeAsset, Throwable t) throws InterruptedException {
        if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
            if (trade.isAlarmOnError()) {
                String subject = String.format("[%s - %s]", trade.getName(), tradeAsset != null ? tradeAsset.getName() : "");
                String content = ExceptionUtils.getRootCause(t).getMessage();
                alarmService.sendAlarm(trade.getAlarmId(), subject, content);
            }
        }
    }

    private void buyTradeAsset(Trade trade, TradeAsset tradeAsset, OrderType orderType, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        TradeClient tradeClient = TradeClientFactory.getClient(trade);
        Order order = Order.builder()
                .orderId(IdGenerator.uuid())
                .orderAt(LocalDateTime.now())
                .orderKind(OrderKind.BUY)
                .orderType(orderType)
                .tradeId(trade.getTradeId())
                .symbol(tradeAsset.getSymbol())
                .assetName(tradeAsset.getName())
                .quantity(quantity)
                .price(price)
                .build();

        // check waiting order exists
        Order waitingOrder = tradeClient.getWaitingOrders().stream()
                .filter(element ->
                        Objects.equals(element.getSymbol(), order.getSymbol())
                                && element.getOrderKind() == order.getOrderKind())
                .findFirst()
                .orElse(null);
        if(waitingOrder != null) {
            // if limit type order, amend order
            if(waitingOrder.getOrderType() == OrderType.LIMIT) {
                waitingOrder.setPrice(price);
                log.info("amend buy order:{}", waitingOrder);
                tradeClient.amendOrder(waitingOrder);
            }
            return;
        }

        // submit buy order
        try {
            log.info("submit buy order:{}", order);
            tradeClient.submitOrder(order);
            if (trade.isAlarmOnOrder()) {
                if (trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                    String subject = String.format("[%s]", trade.getName());
                    String content = String.format("[%s] Buy %s", tradeAsset.getName(), quantity);
                    alarmService.sendAlarm(trade.getAlarmId(), subject, content);
                }
            }
            order.setOrderResult(OrderResult.COMPLETED);
        } catch(Throwable e) {
            order.setOrderResult(OrderResult.FAILED);
            order.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            saveTradeOrder(order);
        }
    }

    private void sellBalanceAsset(Trade trade, BalanceAsset balanceAsset, OrderType orderType, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        TradeClient tradeClient = TradeClientFactory.getClient(trade);
        Order order = Order.builder()
                .orderId(IdGenerator.uuid())
                .orderAt(LocalDateTime.now())
                .orderKind(OrderKind.SELL)
                .orderType(orderType)
                .tradeId(trade.getTradeId())
                .symbol(balanceAsset.getSymbol())
                .assetName(balanceAsset.getName())
                .quantity(quantity)
                .price(price)
                .build();

        // check waiting order exists
        Order waitingOrder = tradeClient.getWaitingOrders().stream()
                .filter(element ->
                        Objects.equals(element.getSymbol(), order.getSymbol())
                                && element.getOrderKind() == order.getOrderKind())
                .findFirst()
                .orElse(null);
        if(waitingOrder != null) {
            // if limit type order, amend order
            if(waitingOrder.getOrderType() == OrderType.LIMIT) {
                waitingOrder.setPrice(price);
                log.info("amend sell order:{}", waitingOrder);
                tradeClient.amendOrder(waitingOrder);
            }
            return;
        }

        // submit sell order
        try {
            log.info("submit sell order:{}", order);
            tradeClient.submitOrder(order);
            if (trade.isAlarmOnOrder()) {
                if (trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                    String subject = String.format("[%s]", trade.getName());
                    String content = String.format("[%s] Sell %s", balanceAsset.getName(), quantity);
                    alarmService.sendAlarm(trade.getAlarmId(), subject, content);
                }
            }
            order.setOrderResult(OrderResult.COMPLETED);
        } catch(Throwable e) {
            order.setOrderResult(OrderResult.FAILED);
            order.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            saveTradeOrder(order);
        }
    }

    private void saveTradeOrder(Order order) {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager, transactionDefinition);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            orderRepository.saveAndFlush(OrderEntity.builder()
                    .orderId(order.getOrderId())
                    .orderAt(order.getOrderAt())
                    .orderKind(order.getOrderKind())
                    .tradeId(order.getTradeId())
                    .symbol(order.getSymbol())
                    .assetName(order.getAssetName())
                    .orderType(order.getOrderType())
                    .quantity(order.getQuantity())
                    .price(order.getPrice())
                    .orderResult(order.getOrderResult())
                    .errorMessage(order.getErrorMessage())
                    .build());
        });
    }

}
