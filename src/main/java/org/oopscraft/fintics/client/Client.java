package org.oopscraft.fintics.client;

import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;

public interface Client {

    AssetIndicator getAssetIndicator(String symbol, AssetType type);

    Balance getBalance();

    void buyAsset(TradeAsset tradeAsset, int quantity, BigDecimal price);

    void sellAsset(BalanceAsset balanceAsset, int quantity, BigDecimal price);

}
