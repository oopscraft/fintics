package org.oopscraft.fintics.trade.order;

import ch.qos.logback.classic.Logger;
import lombok.Getter;
import lombok.Setter;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.dao.OrderEntity;
import org.oopscraft.fintics.dao.OrderRepository;
import org.oopscraft.fintics.model.*;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public abstract class OrderOperator {

    @Setter
    private Trade trade;

    @Setter
    private TradeClient brokerClient;

    @Setter
    private Balance balance;

    @Setter
    private OrderBook orderBook;

    @Setter
    private OrderRepository orderRepository;

    @Setter
    private AlarmService alarmService;

    @Setter
    private PlatformTransactionManager transactionManager;

    @Setter
    protected Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    public abstract void buyTradeAsset(TradeAsset tradeAsset) throws InterruptedException;

    public abstract void sellTradeAsset(TradeAsset balanceAsset) throws InterruptedException;

    public BigDecimal calculateHoldRatioAmount(TradeAsset tradeAsset) {
        return balance.getTotalAmount()
                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL32)
                .multiply(tradeAsset.getHoldRatio())
                .setScale(2, RoundingMode.HALF_UP);
    }

    protected void buyAssetByAmount(Asset asset, BigDecimal amount) throws InterruptedException {
        BigDecimal price = orderBook.getPrice();
        BigDecimal quantity = amount.divide(price, MathContext.DECIMAL32);
        buyAssetByQuantityAndPrice(asset, quantity, price);
    }

    protected void buyAssetByQuantityAndPrice(Asset asset, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        Order order = Order.builder()
                .orderId(IdGenerator.uuid())
                .orderAt(LocalDateTime.now())
                .type(Order.Type.BUY)
                .kind(getTrade().getOrderKind())
                .tradeId(trade.getTradeId())
                .assetId(asset.getAssetId())
                .assetName(asset.getAssetName())
                .quantity(quantity)
                .price(price)
                .build();

        // check waiting order exists
        Order waitingOrder = brokerClient.getWaitingOrders().stream()
                .filter(element ->
                        Objects.equals(element.getSymbol(), order.getSymbol())
                                && element.getType() == order.getType())
                .findFirst()
                .orElse(null);
        if(waitingOrder != null) {
            // if limit type order, amend order
            if(waitingOrder.getKind() == Order.Kind.LIMIT) {
                waitingOrder.setPrice(price);
                log.info("amend buy order:{}", waitingOrder);
                brokerClient.amendOrder(waitingOrder);
            }
            return;
        }

        // submit buy order
        try {
            log.info("submit buy order:{}", order);
            brokerClient.submitOrder(order);
            if (trade.isAlarmOnOrder()) {
                if (trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                    String subject = String.format("[%s]", trade.getTradeName());
                    String content = String.format("[%s] Buy %s", asset.getAssetName(), quantity);
                    alarmService.sendAlarm(trade.getAlarmId(), subject, content);
                }
            }
            order.setResult(Order.Result.COMPLETED);
        } catch(Throwable e) {
            order.setResult(Order.Result.FAILED);
            order.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            saveTradeOrder(order);
        }
    }

    protected void sellAssetByAmount(Asset asset, BigDecimal amount) throws InterruptedException {
        BigDecimal price = getOrderBook().getPrice();
        BigDecimal quantity = amount.divide(price, MathContext.DECIMAL32);
        sellAssetByQuantityAndPrice(asset, quantity, price);
    }

    protected void sellAssetByQuantityAndPrice(Asset asset, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        Order order = Order.builder()
                .orderId(IdGenerator.uuid())
                .orderAt(LocalDateTime.now())
                .type(Order.Type.SELL)
                .kind(getTrade().getOrderKind())
                .tradeId(trade.getTradeId())
                .assetId(asset.getAssetId())
                .assetName(asset.getAssetName())
                .quantity(quantity)
                .price(price)
                .build();

        // check waiting order exists
        Order waitingOrder = brokerClient.getWaitingOrders().stream()
                .filter(element ->
                        Objects.equals(element.getSymbol(), order.getSymbol())
                                && element.getType() == order.getType())
                .findFirst()
                .orElse(null);
        if(waitingOrder != null) {
            // if limit type order, amend order
            if(waitingOrder.getKind() == Order.Kind.LIMIT) {
                waitingOrder.setPrice(price);
                log.info("amend sell order:{}", waitingOrder);
                brokerClient.amendOrder(waitingOrder);
            }
            return;
        }

        // submit sell order
        try {
            log.info("submit sell order:{}", order);
            brokerClient.submitOrder(order);
            if (trade.isAlarmOnOrder()) {
                if (trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                    String subject = String.format("[%s]", trade.getTradeName());
                    String content = String.format("[%s] Sell %s", asset.getAssetName(), quantity);
                    alarmService.sendAlarm(trade.getAlarmId(), subject, content);
                }
            }
            order.setResult(Order.Result.COMPLETED);
        } catch(Throwable e) {
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

}
