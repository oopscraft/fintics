package org.oopscraft.fintics.client.trade;

import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Link;

import java.util.ArrayList;
import java.util.List;

public abstract class KrTradeClientDefinition implements TradeClientDefinition {

    @Override
    public String getMarketId() {
        return "KR";
    }

}
