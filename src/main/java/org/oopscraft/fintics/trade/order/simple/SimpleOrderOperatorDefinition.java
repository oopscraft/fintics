package org.oopscraft.fintics.trade.order.simple;

import org.oopscraft.fintics.trade.order.OrderOperator;
import org.oopscraft.fintics.trade.order.OrderOperatorDefinition;

public class SimpleOrderOperatorDefinition implements OrderOperatorDefinition {

    @Override
    public String getOperatorId() {
        return "SIMPLE";
    }

    @Override
    public String getOperatorName() {
        return "Simple Order Operator";
    }

    @Override
    public Class<? extends OrderOperator> getType() {
        return SimpleOrderOperator.class;
    }

}
