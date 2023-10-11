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

    public abstract AssetIndicator getAssetIndicator(Asset asset);

    public abstract Balance getBalance();

    public abstract void buyAsset(TradeAsset tradeAsset, int quantity);

    public abstract void sellAsset(BalanceAsset balanceAsset, int quantity);

}
