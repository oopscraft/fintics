package org.oopscraft.fintics.client.asset;

import lombok.Getter;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetMeta;

import java.util.List;

public abstract class AssetClient {

    @Getter
    private final AssetClientProperties assetClientProperties;

    protected AssetClient(AssetClientProperties assetClientProperties) {
        this.assetClientProperties = assetClientProperties;
    }

    public abstract List<Asset> getAssets();

    public abstract List<AssetMeta> getAssetMetas(Asset asset);

    public String toAssetId(String market, String symbol) {
        return String.format("%s.%s", market, symbol);
    }

}
