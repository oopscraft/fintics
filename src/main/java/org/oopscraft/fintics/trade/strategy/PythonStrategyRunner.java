package org.oopscraft.fintics.trade.strategy;

import lombok.Builder;
import org.oopscraft.fintics.model.*;
import org.python.util.PythonInterpreter;

import java.time.LocalDateTime;

public class PythonStrategyRunner extends StrategyRunner {

    /**
     * constructor
     *
     * @param strategy     strategy
     * @param variables    variable
     * @param dateTime     date time
     * @param basketAsset basket asset
     * @param tradeAsset   trade asset
     * @param balanceAsset balance asset
     * @param orderBook    order book
     */
    @Builder
    public PythonStrategyRunner(Strategy strategy, String variables, LocalDateTime dateTime, BasketAsset basketAsset, TradeAsset tradeAsset, BalanceAsset balanceAsset, OrderBook orderBook) {
        super(strategy, variables, dateTime, basketAsset, tradeAsset, balanceAsset, orderBook);
    }

    /**
     * TODO jython not support python3 yet.
     * @return strategy result
     */
    @Override
    public StrategyResult run() {
        try (PythonInterpreter interpreter = new PythonInterpreter()) {
            interpreter.exec(strategy.getScript());
        }
        return null;
    }

}
