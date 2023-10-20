package org.oopscraft.fintics.thread;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.Builder;
import org.oopscraft.fintics.model.AssetIndicator;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TradeAssetDecider {

    private final String holdCondition;

    private final AssetIndicator assetIndicator;

    private final Logger logger;

    @Builder
    public TradeAssetDecider(String holdCondition, AssetIndicator assetIndicator, Logger logger) {
        this.holdCondition = holdCondition;
        this.assetIndicator = assetIndicator;
        this.logger = logger;
    }

    public Boolean execute() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(classLoader);
        Binding binding = new Binding();
        binding.setVariable("assetIndicator", assetIndicator);
        binding.setVariable("tool", new Tool());
        binding.setVariable("log", logger);
        GroovyShell groovyShell = new GroovyShell(groovyClassLoader, binding);

        if(holdCondition == null || holdCondition.isBlank()) {
            return null;
        }
        Object result = groovyShell.evaluate(holdCondition);
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
