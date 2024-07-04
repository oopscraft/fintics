package org.oopscraft.fintics.client.asset;

import lombok.Getter;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetNews;

import java.util.List;

public abstract class AssetNewsClient {

    @Getter
    private final AssetNewsClientProperties newsClientProperties;

    public AssetNewsClient(AssetNewsClientProperties newsClientProperties) {
        this.newsClientProperties = newsClientProperties;
    }

    public abstract List<AssetNews> getNewses(Asset asset);

}