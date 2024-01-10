package org.oopscraft.fintics.trade.order;

import org.springframework.beans.factory.Aware;

public interface OrderOperatorDefinition extends Aware {

    String getId();

    String getName();

    Class<? extends OrderOperator> getType();

}
