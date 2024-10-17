package org.oopscraft.fintics.basket;

import ch.qos.logback.classic.Logger;
import lombok.Getter;
import org.oopscraft.arch4j.core.common.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.service.AssetService;
import org.oopscraft.fintics.strategy.StrategyRunner;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

public abstract class BasketScriptRunner {

    @Getter
    protected final Basket basket;

    @Getter
    protected final AssetService assetService;

    @Getter
    protected final OhlcvClient ohlcvClient;

    protected Logger log = (Logger) LoggerFactory.getLogger(StrategyRunner.class);

    protected BasketScriptRunner(Basket basket, AssetService assetService, OhlcvClient ohlcvClient) {
        this.basket = basket;
        this.assetService = assetService;
        this.ohlcvClient = ohlcvClient;
    }

    /**
     * sets logger
     * @param log
     */
    public void setLog(Logger log) {
        this.log = log;
    }

    /**
     * get basket rebalance results
     * @return rebalance results
     */
    public abstract List<BasketRebalanceAsset> run();

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
