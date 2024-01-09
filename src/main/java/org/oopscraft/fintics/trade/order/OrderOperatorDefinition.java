package org.oopscraft.fintics.trade.order;

import org.oopscraft.fintics.client.trade.TradeClient;
import org.springframework.beans.factory.Aware;

public interface OrderOperatorDefinition extends Aware {

    String getOperatorId();

    String getOperatorName();

    Class<? extends OrderOperator> getType();

}
