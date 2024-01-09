package org.oopscraft.fintics.trade.order;

import java.lang.reflect.Constructor;

public class OrderOperatorFactory {

    public static OrderOperator getOrderOperator(OrderOperatorContext orderOperatorContext) {
        OrderOperatorDefinition orderOperatorDefinition = OrderOperatorDefinitionRegistry.getOrderOperatorDefinition(orderOperatorContext.getOperatorId()).orElseThrow();
        try {
            Class<? extends OrderOperator> clientTypeClass = orderOperatorDefinition.getType().asSubclass(OrderOperator.class);
            Constructor<? extends OrderOperator> constructor = clientTypeClass.getConstructor(orderOperatorContext.getClass());
            return constructor.newInstance(orderOperatorContext);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Client constructor not found: " + orderOperatorDefinition.getType(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
