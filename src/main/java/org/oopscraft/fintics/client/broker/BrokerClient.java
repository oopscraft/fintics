package org.oopscraft.fintics.client.broker;

import lombok.Getter;
import org.oopscraft.fintics.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

public abstract class BrokerClient {

    @Getter
    private final BrokerClientDefinition definition;

    @Getter
    private final Properties config;

    public BrokerClient(BrokerClientDefinition definition, Properties config) {
        this.definition = definition;
        this.config = config;
    }

    public abstract boolean isOpened(LocalDateTime dateTime) throws InterruptedException;

    public abstract List<BrokerAsset> getBrokerAssets();

    public abstract List<Ohlcv> getMinuteOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException;

    public abstract List<Ohlcv> getDailyOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException;

    public abstract OrderBook getOrderBook(Asset asset) throws InterruptedException;

    public abstract Balance getBalance() throws InterruptedException;

    public abstract Order submitOrder(Order order) throws InterruptedException;

    public abstract List<Order> getWaitingOrders() throws InterruptedException;

    public abstract Order amendOrder(Order order) throws InterruptedException;

}