package org.oopscraft.fintics.trade.order.rebalance;

import org.oopscraft.fintics.trade.order.OrderOperator;
import org.oopscraft.fintics.trade.order.OrderOperatorDefinition;
import org.springframework.stereotype.Component;

@Component
public class RebalanceOrderOperatorDefinition implements OrderOperatorDefinition {

    @Override
    public String getOrderOperatorId() {
        return "REBALANCE";
    }

    @Override
    public String getOrderOperatorName() {
        return "Rebalance Order Operator";
    }

    @Override
    public Class<? extends OrderOperator> getClassType() {
        return RebalanceOrderOperator.class;
    }

}
