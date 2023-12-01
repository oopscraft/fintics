package org.oopscraft.fintics.client;

import lombok.Getter;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

public abstract class TradeClient {

    @Getter
    private final Properties properties;

    public TradeClient(Properties properties) {
        this.properties = properties;
    }

    public abstract OrderBook getOrderBook(TradeAsset tradeAsset) throws InterruptedException;

    public abstract List<TradeAssetOhlcv> getMinuteOhlcvs(TradeAsset tradeAsset) throws InterruptedException;

    public abstract List<TradeAssetOhlcv> getDailyOhlcvs(TradeAsset asset) throws InterruptedException;

    public abstract Balance getBalance() throws InterruptedException;

    public abstract void buyAsset(TradeAsset tradeAsset, OrderType orderType, BigDecimal quantity, BigDecimal price) throws InterruptedException;

    public abstract void sellAsset(BalanceAsset balanceAsset, OrderType orderType, BigDecimal quantity, BigDecimal price) throws InterruptedException;

}
