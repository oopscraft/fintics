package org.oopscraft.fintics.calculator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CalculatorFactory {

    private final static Map<Class<?>, Class<?>> registry = new LinkedHashMap<>(){{
        put(SmaContext.class, SmaCalculator.class);
        put(EmaContext.class, EmaCalculator.class);
        put(BollingerBandContext.class, BollingerBandCalculator.class);
        put(MacdContext.class, MacdCalculator.class);
        put(RsiContext.class, RsiCalculator.class);
        put(DmiContext.class, DmiCalculator.class);
        put(ObvContext.class, ObvCalculator.class);
        put(ChaikinOscillatorContext.class, ChaikinOscillatorCalculator.class);
        put(DisparityIndexContext.class, DisparityIndexCalculator.class);
        put(StochasticSlowContext.class, StochasticSlowCalculator.class);
    }};

    public static <C extends CalculateContext, R extends CalculateResult, T extends Calculator<C,R>> T getCalculator(C context) {
        Class<?> calculatorType = registry.get(context.getClass());
        Calculator<?,?> calculator;
        try {
            Constructor<?> constructor = calculatorType.getConstructor(context.getClass());
            calculator = (Calculator<?,?>) constructor.newInstance(context);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return (T) calculator;
    }

}
