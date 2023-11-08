package org.oopscraft.fintics.client.trade;

import lombok.Getter;
import org.oopscraft.fintics.model.*;

import java.util.List;
import java.util.Properties;

public abstract class TradeClient {

    @Getter
    private final Properties properties;

    public TradeClient(Properties properties) {
        this.properties = properties;
    }

    public abstract OrderBook getOrderBook(Asset asset) throws InterruptedException;

    public abstract List<Ohlcv> getMinuteOhlcvs(Asset asset) throws InterruptedException;

    public abstract List<Ohlcv> getDailyOhlcvs(Asset asset) throws InterruptedException;

    public abstract Balance getBalance() throws InterruptedException;

    public abstract void buyAsset(TradeAsset tradeAsset, int quantity) throws InterruptedException;

    public abstract void sellAsset(BalanceAsset balanceAsset, int quantity) throws InterruptedException;

}
