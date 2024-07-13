package org.oopscraft.fintics.trade.strategy;

import ch.qos.logback.classic.Logger;
import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.model.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Properties;

public abstract class StrategyRunner {

    protected final Strategy strategy;

    protected final String variables;

    protected final LocalDateTime dateTime;

    protected final TradeAsset tradeAsset;

    protected final OrderBook orderBook;

    protected final Balance balance;

    protected final BalanceAsset balanceAsset;

    protected Logger log = (Logger) LoggerFactory.getLogger(StrategyRunner.class);

    /**
     * constructor
     * @param strategy strategy
     * @param variables variable
     * @param dateTime date time
     * @param tradeAsset trade asset
     * @param orderBook order book
     * @param balance balance
     * @param balanceAsset balance asset
     */
    public StrategyRunner(
            Strategy strategy,
            String variables,
            LocalDateTime dateTime,
            TradeAsset tradeAsset,
            OrderBook orderBook,
            Balance balance,
            BalanceAsset balanceAsset
    ) {
        this.strategy  = strategy;
        this.variables = variables;
        this.dateTime = dateTime;
        this.tradeAsset = tradeAsset;
        this.orderBook = orderBook;
        this.balance = balance;
        this.balanceAsset = balanceAsset;
    }

    /**
     * sets logger
     * @param log
     */
    public void setLog(Logger log) {
        this.log = log;
    }

    /**
     * runs strategy script
     * @return result of execution
     */
    public abstract StrategyResult run();

    /**
     * loads properties string to properties object
     * @param propertiesString property string
     * @return properties
     */
    Properties loadRuleConfigAsProperties(String propertiesString) {
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