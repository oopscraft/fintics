package org.oopscraft.fintics.simulate;

import lombok.Builder;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.sql.Array;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SimulateTradeClient extends TradeClient {

    private final Balance balance;

    public SimulateTradeClient(Properties properties) {
        super(properties);
        this.balance = Balance.builder()
                .totalAmount(BigDecimal.valueOf(1_000_000))
                .build();
    }

    @Override
    public boolean isOpened(LocalDateTime dateTime) throws InterruptedException {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    @Override
    public List<Ohlcv> getMinuteOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        return new ArrayList<>();
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        return new ArrayList<>();
    }

    @Override
    public OrderBook getOrderBook(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        return OrderBook.builder()
                .build();
    }

    @Override
    public Balance getBalance() throws InterruptedException {
        return balance;
    }

    @Override
    public Order submitOrder(Order order) throws InterruptedException {
        return order;
    }

    @Override
    public List<Order> getWaitingOrders() throws InterruptedException {
        return new ArrayList<>();
    }

    @Override
    public Order amendOrder(Order order) throws InterruptedException {
        return order;
    }

}
