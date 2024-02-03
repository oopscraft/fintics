package org.oopscraft.fintics.client.trade;

import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Link;
import org.springframework.beans.factory.Aware;

import java.util.List;

public interface TradeClientDefinition extends Aware {

    String getTradeClientId();

    String getTradeClientName();

    String getExchangeId();

    List<Link> getAssetLinks(Asset asset);

    Class<? extends TradeClient> getClassType();

    String getConfigTemplate();

}
