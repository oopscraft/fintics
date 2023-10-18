package org.oopscraft.fintics.thread;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.calculator.EmaCalculator;
import org.oopscraft.fintics.calculator.SmaCalculator;
import org.oopscraft.fintics.model.AssetIndicator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Builder
@Slf4j
public class TradeAssetDecider {

    private String holdCondition;

    private AssetIndicator assetIndicator;

    public Boolean execute() {
        Binding binding = new Binding();
        binding.setVariable("log", log);
        binding.setVariable("tool", new Tool());
        binding.setVariable("assetIndicator", assetIndicator);
        GroovyShell groovyShell = new GroovyShell(binding);
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

        public List<Double> sma(List<Double> values, int period) {
            List<Double> series = new ArrayList<>(values);
            Collections.reverse(series);
            List<Double> smas = SmaCalculator.of(series, period).getSmas();
            Collections.reverse(smas);
            return smas;
        }

        public List<Double> ema(List<Double> values, int period) {
            List<Double> series = new ArrayList<>(values);
            Collections.reverse(series);
            List<Double> emas = EmaCalculator.of(series, period).getEmas();
            Collections.reverse(emas);
            return emas;
        }

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
