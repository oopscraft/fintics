package org.oopscraft.fintics.trade.order;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class OrderOperatorFactory implements BeanPostProcessor {

    @Getter
    private static final List<OrderOperatorDefinition> orderOperatorDefinitions = new ArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof OrderOperatorDefinition) {
            orderOperatorDefinitions.add((OrderOperatorDefinition) bean);
        }
        return bean;
    }

    public OrderOperator getObject(OrderOperatorContext context) {
        OrderOperatorDefinition orderOperatorDefinition = orderOperatorDefinitions.stream()
                .filter(clientDefinition -> Objects.equals(clientDefinition.getOrderOperatorId(), context.getId()))
                .findFirst()
                .orElseThrow();
        try {
            Class<? extends OrderOperator> clientTypeClass = orderOperatorDefinition.getClassType().asSubclass(OrderOperator.class);
            Constructor<? extends OrderOperator> constructor = clientTypeClass.getConstructor(context.getClass());
            return constructor.newInstance(context);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Client constructor not found: " + orderOperatorDefinition.getClassType(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
