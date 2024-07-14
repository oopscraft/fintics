package org.oopscraft.fintics.trade.basket;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.Builder;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.service.AssetService;

import java.util.List;

public class GroovyBasketRebalance extends BasketRebalance {

    @Builder
    public GroovyBasketRebalance(Basket basket, AssetService assetService, OhlcvClient ohlcvClient) {
        super(basket, assetService, ohlcvClient);
    }

    @Override
    public List<BasketChange> getChanges() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(classLoader);
        Binding binding = new Binding();
        binding.setVariable("basket", basket);
        binding.setVariable("assetService", assetService);
        binding.setVariable("ohlcvClient", ohlcvClient);
        GroovyShell groovyShell = new GroovyShell(groovyClassLoader, binding);
        Object result = groovyShell.evaluate(
                "import " + BasketChange.class.getName() + '\n' +
                        basket.getScript()
        );
        if (result != null) {
            return (List<BasketChange>) result;
        }
        return null;
    }

}
