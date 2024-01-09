package org.oopscraft.fintics.trade.order;

import ch.qos.logback.classic.Logger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.dao.OrderEntity;
import org.oopscraft.fintics.dao.OrderRepository;
import org.oopscraft.fintics.model.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public abstract class OrderOperator {

    private final TradeClient tradeClient;

    private final Trade trade;

    private final Balance balance;

    private final OrderBook orderBook;

    protected final Logger log;

    private final OrderRepository orderRepository;

    private final AlarmService alarmService;

    private final PlatformTransactionManager transactionManager;

    public abstract void buyTradeAsset(TradeAsset tradeAsset) throws InterruptedException;

    public abstract void sellTradeAsset(TradeAsset balanceAsset) throws InterruptedException;

    public OrderOperator(OrderOperatorContext context) {
        this.tradeClient = context.getTradeClient();
        this.trade = context.getTrade();
        this.balance = context.getBalance();
        this.orderBook = context.getOrderBook();
        this.log = context.getLog();
        this.orderRepository = context.getApplicationContext().getBean(OrderRepository.class);
        this.alarmService = context.getApplicationContext().getBean(AlarmService.class);
        this.transactionManager = context.getApplicationContext().getBean(PlatformTransactionManager.class);
    }

    protected void buyAsset(Asset asset, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        Order order = Order.builder()
                .orderId(IdGenerator.uuid())
                .orderAt(LocalDateTime.now())
                .orderType(OrderType.BUY)
                .orderKind(getTrade().getOrderKind())
                .tradeId(trade.getTradeId())
                .symbol(asset.getSymbol())
                .assetName(asset.getName())
                .quantity(quantity)
                .price(price)
                .build();

        // check waiting order exists
        Order waitingOrder = tradeClient.getWaitingOrders().stream()
                .filter(element ->
                        Objects.equals(element.getSymbol(), order.getSymbol())
                                && element.getOrderType() == order.getOrderType())
                .findFirst()
                .orElse(null);
        if(waitingOrder != null) {
            // if limit type order, amend order
            if(waitingOrder.getOrderKind() == OrderKind.LIMIT) {
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
                    String content = String.format("[%s] Buy %s", asset.getName(), quantity);
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

    protected void sellAsset(Asset asset, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        Order order = Order.builder()
                .orderId(IdGenerator.uuid())
                .orderAt(LocalDateTime.now())
                .orderType(OrderType.SELL)
                .orderKind(getTrade().getOrderKind())
                .tradeId(trade.getTradeId())
                .symbol(asset.getSymbol())
                .assetName(asset.getName())
                .quantity(quantity)
                .price(price)
                .build();

        // check waiting order exists
        Order waitingOrder = tradeClient.getWaitingOrders().stream()
                .filter(element ->
                        Objects.equals(element.getSymbol(), order.getSymbol())
                                && element.getOrderType() == order.getOrderType())
                .findFirst()
                .orElse(null);
        if(waitingOrder != null) {
            // if limit type order, amend order
            if(waitingOrder.getOrderKind() == OrderKind.LIMIT) {
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
                    String content = String.format("[%s] Sell %s", asset.getName(), quantity);
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
        transactionTemplate.executeWithoutResult(transactionStatus ->
                orderRepository.saveAndFlush(OrderEntity.builder()
                        .orderId(order.getOrderId())
                        .orderAt(order.getOrderAt())
                        .orderType(order.getOrderType())
                        .tradeId(order.getTradeId())
                        .symbol(order.getSymbol())
                        .assetName(order.getAssetName())
                        .orderKind(order.getOrderKind())
                        .quantity(order.getQuantity())
                        .price(order.getPrice())
                        .orderResult(order.getOrderResult())
                        .errorMessage(order.getErrorMessage())
                        .build()));
    }

}
