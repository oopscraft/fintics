package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.Builder;
import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.model.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Properties;

@Builder
public class StrategyExecutor {

    private final Strategy strategy;

    private final String variables;

    private final LocalDateTime dateTime;

    private final TradeAsset tradeAsset;

    private final OrderBook orderBook;

    private final Balance balance;

    private final BalanceAsset balanceAsset;

    @Builder.Default
    private Logger log = (Logger) LoggerFactory.getLogger(StrategyExecutor.class);

    /**
     * sets logger
     * @param log
     */
    public void setLog(Logger log) {
        this.log = log;
    }

    /**
     * executes strategy script
     * @return result of execution
     */
    public StrategyResult execute() {
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

    /**
     * loads properties string to properties object
     * @param propertiesString property string
     * @return properties
     */
    private Properties loadRuleConfigAsProperties(String propertiesString) {
        Properties properties = new Properties();
        if (propertiesString != null && !propertiesString.isBlank()) {
            try {
                properties.load(new StringReader(propertiesString));
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid properties string", e);
            }
            properties = PbePropertiesUtil.decode(properties);
            properties = PbePropertiesUtil.unwrapDecryptedMark(properties);
        }
        return properties;
    }
}