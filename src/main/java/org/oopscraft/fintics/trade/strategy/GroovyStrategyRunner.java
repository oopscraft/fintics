package org.oopscraft.fintics.trade.strategy;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.Builder;
import org.oopscraft.fintics.model.*;

import java.time.LocalDateTime;

public class GroovyStrategyRunner extends StrategyRunner {

    /**
     * constructor
     * @param strategy     strategy
     * @param variables    variable
     * @param dateTime     date time
     * @param tradeAsset   trade asset
     * @param orderBook    order book
     * @param balance      balance
     * @param balanceAsset balance asset
     */
    @Builder
    public GroovyStrategyRunner(Strategy strategy, String variables, LocalDateTime dateTime, TradeAsset tradeAsset, OrderBook orderBook, Balance balance, BalanceAsset balanceAsset) {
        super(strategy, variables, dateTime, tradeAsset, orderBook, balance, balanceAsset);
    }

    /**
     * executes strategy script
     * @return result of execution
     */
    @Override
    public StrategyResult run() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(classLoader);
        Binding binding = new Binding();
        binding.setVariable("variables", loadRuleConfigAsProperties(variables));
        binding.setVariable("log", log);
        binding.setVariable("dateTime", dateTime);
        binding.setVariable("tradeAsset", tradeAsset);
        binding.setVariable("orderBook", orderBook);
        binding.setVariable("balance", balance);
        binding.setVariable("balanceAsset", balanceAsset);
        GroovyShell groovyShell = new GroovyShell(groovyClassLoader, binding);
        Object result = groovyShell.evaluate(
                "import " + StrategyResult.class.getName() + '\n' +
                        "import " + StrategyResult.Action.class.getName().replaceAll("\\$",".") + '\n' +
                        strategy.getScript()
        );
        if (result != null) {
            return (StrategyResult) result;
        }
        return null;
    }

}
