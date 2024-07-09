package org.oopscraft.fintics.client.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.asset.market.KrAssetClient;
import org.oopscraft.fintics.client.asset.market.UsAssetClient;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetMeta;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "fintics", name = "asset-client.class-name", havingValue="org.oopscraft.fintics.client.asset.SimpleAssetClient")
@Slf4j
public class SimpleAssetClient extends AssetClient {

    private final List<AssetClient> assetClients = new ArrayList<>();

    protected SimpleAssetClient(AssetClientProperties assetClientProperties, ObjectMapper objectMapper) {
        super(assetClientProperties);
        assetClients.add(new UsAssetClient(assetClientProperties, objectMapper));
        assetClients.add(new KrAssetClient(assetClientProperties));
    }

    @Override
    public List<Asset> getAssets() {
        List<Asset> assets = new ArrayList<>();
        for(AssetClient assetClient : assetClients) {
            try {
                assets.addAll(assetClient.getAssets());
            } catch (Throwable e) {
                log.warn(e.getMessage());
            }
        }
        return assets;
    }

    @Override
    public List<AssetMeta> getAssetMetas(Asset asset) {
        List<AssetMeta> assetMetas = new ArrayList<>();
        for(AssetClient assetClient : assetClients) {
            try {
                assetMetas.addAll(assetClient.getAssetMetas(asset));
            } catch (Throwable e) {
                log.warn(e.getMessage());
            }
        }
        return assetMetas;
    }
}
