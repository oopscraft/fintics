package org.oopscraft.fintics.client;

import lombok.Getter;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Properties;

public abstract class Client {

    @Getter
    private final Properties properties;

    public Client(Properties properties) {
        this.properties = properties;
    }

    public abstract AssetIndicator getAssetIndicator(String symbol, AssetType type);

    public abstract Balance getBalance();

    public abstract void buyAsset(TradeAsset tradeAsset, BigDecimal price, int quantity);

    public abstract void sellAsset(BalanceAsset balanceAsset, BigDecimal price, int quantity);

}
