package org.oopscraft.fintics.trade.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.dao.OrderRepository;
import org.oopscraft.fintics.model.Balance;
import org.oopscraft.fintics.model.OrderBook;
import org.oopscraft.fintics.model.Trade;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OrderOperatorFactory implements BeanPostProcessor {

    @Getter
    private final List<OrderOperatorDefinition> orderOperatorDefinitions = new ArrayList<>();

    private final PlatformTransactionManager transactionManager;

    private final OrderRepository orderRepository;

    private final AlarmService alarmService;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof OrderOperatorDefinition) {
            orderOperatorDefinitions.add((OrderOperatorDefinition) bean);
        }
        return bean;
    }

    public OrderOperator getObject(Trade trade, BrokerClient brokerClient, Balance balance, OrderBook orderBook) {
        OrderOperatorDefinition orderOperatorDefinition = orderOperatorDefinitions.stream()
                .filter(clientDefinition -> Objects.equals(clientDefinition.getOrderOperatorId(), trade.getOrderOperatorId()))
                .findFirst()
                .orElseThrow();
        try {
            Class<? extends OrderOperator> clientTypeClass = orderOperatorDefinition.getClassType().asSubclass(OrderOperator.class);
            Constructor<? extends OrderOperator> constructor = clientTypeClass.getConstructor();
            OrderOperator orderOperator = constructor.newInstance();
            orderOperator.setTrade(trade);
            orderOperator.setBrokerClient(brokerClient);
            orderOperator.setBalance(balance);
            orderOperator.setOrderBook(orderBook);
            orderOperator.setTransactionManager(transactionManager);
            orderOperator.setOrderRepository(orderRepository);
            orderOperator.setAlarmService(alarmService);
            return orderOperator;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Client constructor not found: " + orderOperatorDefinition.getClassType(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
