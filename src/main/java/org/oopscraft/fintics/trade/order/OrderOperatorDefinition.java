package org.oopscraft.fintics.trade.order;

import org.springframework.beans.factory.Aware;

public interface OrderOperatorDefinition extends Aware {

    String getOrderOperatorId();

    String getOrderOperatorName();

    Class<? extends OrderOperator> getClassType();

}
