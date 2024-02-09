package org.oopscraft.fintics.client.trade;

import org.springframework.beans.factory.Aware;

public interface TradeClientDefinition extends Aware {

    String getTradeClientId();

    String getTradeClientName();

    String getMarket();

    Class<? extends TradeClient> getClassType();

    String getConfigTemplate();

}
