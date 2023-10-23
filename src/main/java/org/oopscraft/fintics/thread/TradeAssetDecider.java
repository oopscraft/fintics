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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Builder
@Getter
public class TradeAssetDecider {

    private final Trade trade;

    private final TradeAsset tradeAsset;

    private final AssetIndicator assetIndicator;

    private final Logger logger;

    private final boolean firstTrade;

    private final boolean lastTrade;

    public Boolean execute() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(classLoader);
        Binding binding = new Binding();
        binding.setVariable("assetIndicator", assetIndicator);
        binding.setVariable("tool", new Tool());
        binding.setVariable("log", logger);
        binding.setVariable("firstTrade", firstTrade);
        binding.setVariable("lastTrade", lastTrade);
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

    public static class Tool {

        public Double slope(List<Double> values, int period) {
            List<Double> periodValues = values.subList(
                    0,
                    Math.min(period, values.size())
            );
            List<Double> series = new ArrayList<>(periodValues);
            Collections.reverse(series);

            // check empty
            if(series.isEmpty()) {
                return 0.0;
            }

            // sum
            BigDecimal sum = BigDecimal.ZERO;
            for (int i = 0; i < series.size(); i++) {
                BigDecimal change = BigDecimal.valueOf(series.get(i))
                        .subtract(BigDecimal.valueOf(series.get(Math.max(i-1,0))));
                sum = sum.add(change);
            }

            // average
            return sum.divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL128)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

    }

}
