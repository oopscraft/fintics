package org.oopscraft.fintics.client.trade;

import org.springframework.beans.factory.Aware;

public interface TradeClientDefinition extends Aware {

    String getTradeClientId();

    String getTradeClientName();

    Class<? extends TradeClient> getClassType();

    String getConfigTemplate();

}
