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
     * @param tradeAsset   trade asset
     * @param orderBook    order book
     * @param balance      balance
     * @param balanceAsset balance asset
     */
    @Builder
    public PythonStrategyRunner(Strategy strategy, String variables, LocalDateTime dateTime, TradeAsset tradeAsset, OrderBook orderBook, Balance balance, BalanceAsset balanceAsset) {
        super(strategy, variables, dateTime, tradeAsset, orderBook, balance, balanceAsset);
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
