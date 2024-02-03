package org.oopscraft.fintics.client.trade;

import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Link;

import java.util.ArrayList;
import java.util.List;

public abstract class KrTradeClientDefinition implements TradeClientDefinition {

    @Override
    public String getExchangeId() {
        return "KR";
    }

    @Override
    public List<Link> getAssetLinks(Asset asset) {
        return new ArrayList<>() {{
            add(Link.of("Naver", "https://finance.naver.com/item/main.naver?code=" + asset.getSymbol()));
        }};
    }

}
