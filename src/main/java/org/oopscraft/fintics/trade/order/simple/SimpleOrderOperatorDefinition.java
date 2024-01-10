package org.oopscraft.fintics.trade.order.simple;

import org.oopscraft.fintics.trade.order.OrderOperator;
import org.oopscraft.fintics.trade.order.OrderOperatorDefinition;
import org.springframework.stereotype.Component;

@Component
public class SimpleOrderOperatorDefinition implements OrderOperatorDefinition {

    @Override
    public String getId() {
        return "SIMPLE";
    }

    @Override
    public String getName() {
        return "Simple Order Operator";
    }

    @Override
    public Class<? extends OrderOperator> getType() {
        return SimpleOrderOperator.class;
    }

}
