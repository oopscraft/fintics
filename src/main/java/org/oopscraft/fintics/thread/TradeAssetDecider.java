package org.oopscraft.fintics.thread;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Builder
@Getter
public class TradeAssetDecider {

    private final Trade trade;

    private final TradeAsset tradeAsset;

    private final AssetIndicator assetIndicator;

    private final LocalDateTime dateTime;

    private final Logger logger;

    public Boolean execute() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(classLoader);
        Binding binding = new Binding();
        binding.setVariable("assetIndicator", assetIndicator);
        binding.setVariable("tool", new Tool());
        binding.setVariable("dateTime", dateTime);
        binding.setVariable("log", logger);
        GroovyShell groovyShell = new GroovyShell(groovyClassLoader, binding);

        if(trade.getHoldCondition() == null || trade.getHoldCondition().isBlank()) {
            return null;
        }
        Object result = groovyShell.evaluate(trade.getHoldCondition());
        if(result == null) {
            return null;
        }
        return (Boolean) result;
    }

}
