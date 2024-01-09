package org.oopscraft.fintics.trade.order;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class OrderOperatorDefinitionRegistry implements BeanPostProcessor {

    @Getter
    private static final List<OrderOperatorDefinition> orderOperatorDefinitions = new ArrayList<>();

    public void add(OrderOperatorDefinition orderOperatorDefinition) {
        orderOperatorDefinitions.add(orderOperatorDefinition);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof OrderOperatorDefinition) {
            orderOperatorDefinitions.add((OrderOperatorDefinition) bean);
        }
        return bean;
    }

    public static Optional<OrderOperatorDefinition> getOrderOperatorDefinition(String operatorId) {
        return orderOperatorDefinitions.stream()
                .filter(clientDefinition -> Objects.equals(clientDefinition.getOperatorId(), operatorId))
                .findFirst();
    }

}
