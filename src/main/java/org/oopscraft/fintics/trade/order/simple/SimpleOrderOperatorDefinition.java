package org.oopscraft.fintics.trade.order.simple;

import org.oopscraft.fintics.trade.order.OrderOperator;
import org.oopscraft.fintics.trade.order.OrderOperatorDefinition;
import org.springframework.stereotype.Component;

@Component
public class SimpleOrderOperatorDefinition implements OrderOperatorDefinition {

    @Override
    public String getOrderOperatorId() {
        return "SIMPLE";
    }

    @Override
    public String getOrderOperatorName() {
        return "Simple Order Operator";
    }

    @Override
    public Class<? extends OrderOperator> getClassType() {
        return SimpleOrderOperator.class;
    }

}
