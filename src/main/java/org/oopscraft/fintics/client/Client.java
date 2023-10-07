package org.oopscraft.fintics.client;

import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;

public interface Client {

    AssetIndicator getAssetIndicator(Asset asset);

    Balance getBalance();

    void buyAsset(Asset asset, int quantity, BigDecimal price);

    void sellAsset(BalanceAsset balanceAsset, int quantity, BigDecimal price);

}
