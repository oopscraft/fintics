package org.oopscraft.fintics.client.asset;

import org.oopscraft.fintics.model.Asset;

import java.util.List;

public interface AssetClient {

    List<Asset> getStockAssets(int offset, int limit);

    List<Asset> getEtfAssets(int offset, int limit);

}
