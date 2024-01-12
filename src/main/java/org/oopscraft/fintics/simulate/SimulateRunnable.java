package org.oopscraft.fintics.simulate;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import org.oopscraft.fintics.model.Simulate;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.trade.TradeExecutor;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SimulateRunnable implements Runnable {

    private final Simulate simulate;

    private final ApplicationContext applicationContext;

    private final Logger log;

    private final SimulateIndiceClient simulateIndiceClient;

    private final SimulateTradeClient simulateTradeClient;

    @Builder
    public SimulateRunnable(Simulate simulate, SimulateIndiceClient simulateIndiceClient, SimulateTradeClient simulateTradeClient, ApplicationContext applicationContext, Logger log) {
        this.simulate = simulate;
        this.simulateIndiceClient = simulateIndiceClient;
        this.simulateTradeClient = simulateTradeClient;
        this.applicationContext = applicationContext;
        this.log = log;
    }

    @Override
    public void run() {
        Trade trade = simulate.getTrade();
        LocalDateTime dateTimeFrom = simulate.getDateTimeFrom();
        LocalDateTime dateTimeTo = simulate.getDateTimeTo();

        // invest amount
        BigDecimal investAmount = simulate.getInvestAmount();
        simulateTradeClient.deposit(investAmount);

        TradeExecutor tradeExecutor = TradeExecutor.builder()
                .applicationContext(applicationContext)
                .log(log)
                .build();

        for(LocalDateTime dateTime = dateTimeFrom.plusMinutes(1); dateTime.isBefore(dateTimeTo); dateTime = dateTime.plusMinutes(1)) {
            // check start and end time
            if(dateTime.toLocalTime().isBefore(trade.getStartAt()) || dateTime.toLocalTime().isAfter(trade.getEndAt())) {
                continue;
            }

            log.info("== dateTime:{}", dateTime);
            try {
                simulateIndiceClient.setDateTime(dateTime);
                simulateTradeClient.setDateTime(dateTime);
                tradeExecutor.execute(trade, dateTime, simulateIndiceClient, simulateTradeClient);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

}
