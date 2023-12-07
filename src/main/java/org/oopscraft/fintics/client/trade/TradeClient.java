package org.oopscraft.fintics.client.trade;

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

    public abstract OrderBook getOrderBook(Asset asset) throws InterruptedException;

    public abstract List<Ohlcv> getMinuteOhlcvs(Asset asset) throws InterruptedException;

    public abstract List<Ohlcv> getDailyOhlcvs(Asset asset) throws InterruptedException;

    public abstract Balance getBalance() throws InterruptedException;

    public abstract void buyAsset(Asset asset, OrderType orderType, BigDecimal quantity, BigDecimal price) throws InterruptedException;

    public abstract void sellAsset(Asset asset, OrderType orderType, BigDecimal quantity, BigDecimal price) throws InterruptedException;

}
