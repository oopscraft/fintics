package org.oopscraft.fintics.client.broker;

import lombok.Getter;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

public abstract class BrokerClient {

    @Getter
    private final BrokerClientDefinition definition;

    @Getter
    private final Properties properties;

    public BrokerClient(BrokerClientDefinition definition, Properties properties) {
        this.definition = definition;
        this.properties = properties;
    }

    public abstract boolean isOpened(LocalDateTime dateTime) throws InterruptedException;

    public abstract List<Asset> getAssets();

    public abstract List<Ohlcv> getMinuteOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException;

    public abstract List<Ohlcv> getDailyOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException;

    public abstract OrderBook getOrderBook(Asset asset) throws InterruptedException;

    public abstract BigDecimal getTickPrice(Asset asset, BigDecimal price) throws InterruptedException;

    public abstract BigDecimal getMinimumOrderQuantity() throws InterruptedException;

    public abstract Balance getBalance() throws InterruptedException;

    public abstract Order submitOrder(Asset asset, Order order) throws InterruptedException;

    public abstract List<Order> getWaitingOrders() throws InterruptedException;

    public abstract Order amendOrder(Asset asset, Order order) throws InterruptedException;

    public final String toAssetId(String symbol) {
        return String.format("%s.%s", this.definition.getMarket(), symbol);
    }

}
