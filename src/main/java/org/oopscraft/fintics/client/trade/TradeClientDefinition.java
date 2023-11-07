package org.oopscraft.fintics.client.trade;

import org.springframework.beans.factory.Aware;

public interface TradeClientDefinition extends Aware {

    Class<? extends TradeClient> getType();

    String getName();

    String getPropertiesTemplate();

}
