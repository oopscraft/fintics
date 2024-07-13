package org.oopscraft.fintics.trade.strategy;

import org.oopscraft.fintics.model.Strategy;
import org.springframework.stereotype.Component;

@Component
public class StrategyRunnerFactory {

    /**
     * gets object
     * @param context strategy runner context
     * @return strategy runner
     */
    public StrategyRunner getObject(StrategyRunnerContext context) {
        Strategy strategy = context.getStrategy();
        switch (strategy.getLanguage()) {
            case GROOVY -> {
                return GroovyStrategyRunner.builder()
                        .strategy(context.getStrategy())
                        .variables(context.getVariables())
                        .dateTime(context.getDateTime())
                        .tradeAsset(context.getTradeAsset())
                        .orderBook(context.getOrderBook())
                        .balance(context.getBalance())
                        .balanceAsset(context.getBalanceAsset())
                        .build();
            }
            case PYTHON -> {
                return PythonStrategyRunner.builder()
                        .strategy(context.getStrategy())
                        .variables(context.getVariables())
                        .dateTime(context.getDateTime())
                        .tradeAsset(context.getTradeAsset())
                        .orderBook(context.getOrderBook())
                        .balance(context.getBalance())
                        .balanceAsset(context.getBalanceAsset())
                        .build();
            }
            default -> throw new RuntimeException("invalid strategy.language");
        }
    }

}
