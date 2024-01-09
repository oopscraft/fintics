package org.oopscraft.fintics.client.trade;

import org.springframework.beans.factory.Aware;

public interface TradeClientDefinition extends Aware {

    String getClientId();

    String getClientName();

    Class<? extends TradeClient> getType();

    String getPropertiesTemplate();

}
