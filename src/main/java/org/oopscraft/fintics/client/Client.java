package org.oopscraft.fintics.client;

import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;

public interface Client {

    Balance getBalance();

    AssetIndicator getAssetIndicator(Asset asset);

    void buyBasketAsset(BasketAsset basketAsset, int quantity, BigDecimal price);

    void sellBalanceAsset(BalanceAsset balanceAsset, int quantity, BigDecimal price);

}
