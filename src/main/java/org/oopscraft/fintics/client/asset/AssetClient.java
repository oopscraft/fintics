package org.oopscraft.fintics.client.asset;

import lombok.Getter;
import org.oopscraft.arch4j.core.common.support.RestTemplateBuilder;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetMeta;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public abstract class AssetClient {

    @Getter
    private final AssetClientProperties assetClientProperties;

    protected AssetClient(AssetClientProperties assetClientProperties) {
        this.assetClientProperties = assetClientProperties;
    }

    public abstract List<Asset> getAssets();

    public abstract boolean isSupported(Asset asset);

    public abstract List<AssetMeta> getAssetMetas(Asset asset);

    public String toAssetId(String market, String symbol) {
        return String.format("%s.%s", market, symbol);
    }

    public RestTemplate getRestTemplate() {
        return RestTemplateBuilder.create()
                .retryCount(3)
                .build();
    }

}
