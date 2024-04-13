package org.oopscraft.fintics.indicator;

import org.oopscraft.fintics.indicator.atr.AtrCalculator;
import org.oopscraft.fintics.indicator.atr.AtrContext;
import org.oopscraft.fintics.indicator.bollingerband.BollingerBandCalculator;
import org.oopscraft.fintics.indicator.bollingerband.BollingerBandContext;
import org.oopscraft.fintics.indicator.cci.CciCalculator;
import org.oopscraft.fintics.indicator.cci.CciContext;
import org.oopscraft.fintics.indicator.chaikinoscillator.ChaikinOscillatorCalculator;
import org.oopscraft.fintics.indicator.chaikinoscillator.ChaikinOscillatorContext;
import org.oopscraft.fintics.indicator.dmi.DmiCalculator;
import org.oopscraft.fintics.indicator.dmi.DmiContext;
import org.oopscraft.fintics.indicator.ema.EmaCalculator;
import org.oopscraft.fintics.indicator.ema.EmaContext;
import org.oopscraft.fintics.indicator.macd.MacdCalculator;
import org.oopscraft.fintics.indicator.macd.MacdContext;
import org.oopscraft.fintics.indicator.obv.ObvCalculator;
import org.oopscraft.fintics.indicator.obv.ObvContext;
import org.oopscraft.fintics.indicator.rsi.RsiCalculator;
import org.oopscraft.fintics.indicator.rsi.RsiContext;
import org.oopscraft.fintics.indicator.sma.SmaCalculator;
import org.oopscraft.fintics.indicator.sma.SmaContext;
import org.oopscraft.fintics.indicator.stochasticslow.StochasticSlowCalculator;
import org.oopscraft.fintics.indicator.stochasticslow.StochasticSlowContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class IndicatorCalculatorFactory {

    private final static Map<Class<?>, Class<?>> registry = new LinkedHashMap<>(){{
        put(SmaContext.class, SmaCalculator.class);
        put(EmaContext.class, EmaCalculator.class);
        put(BollingerBandContext.class, BollingerBandCalculator.class);
        put(MacdContext.class, MacdCalculator.class);
        put(RsiContext.class, RsiCalculator.class);
        put(DmiContext.class, DmiCalculator.class);
        put(ObvContext.class, ObvCalculator.class);
        put(ChaikinOscillatorContext.class, ChaikinOscillatorCalculator.class);
        put(AtrContext.class, AtrCalculator.class);
        put(CciContext.class, CciCalculator.class);
        put(StochasticSlowContext.class, StochasticSlowCalculator.class);
    }};

    public static <C extends IndicatorContext, R extends Indicator, T extends IndicatorCalculator<C,R>> T getIndicator(C context) {
        Class<?> calculatorType = registry.get(context.getClass());
        IndicatorCalculator<?,?> calculator;
        try {
            Constructor<?> constructor = calculatorType.getConstructor(context.getClass());
            calculator = (IndicatorCalculator<?,?>) constructor.newInstance(context);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return (T) calculator;
    }

}
