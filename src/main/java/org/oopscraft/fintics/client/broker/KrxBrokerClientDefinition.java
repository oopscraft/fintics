package org.oopscraft.fintics.client.broker;

import org.oopscraft.fintics.model.Asset;

import java.util.ArrayList;
import java.util.List;

public abstract class KrxBrokerClientDefinition implements BrokerClientDefinition {

    @Override
    public List<Asset.Link> getAssetLinks(Asset asset) {
        return new ArrayList<>() {{
            add(Asset.Link.of("Naver", "https://finance.naver.com/item/main.naver?code=" + asset.getAssetId()));
        }};
    }

}
