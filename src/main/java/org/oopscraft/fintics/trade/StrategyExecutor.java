package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.Builder;
import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.Balance;
import org.oopscraft.fintics.model.IndiceIndicator;
import org.oopscraft.fintics.model.OrderBook;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class StrategyExecutor {

    private final String ruleConfig;

    private final String ruleScript;

    private final LocalDateTime dateTime;

    private final OrderBook orderBook;

    private final Balance balance;

    private final Map<String, IndiceIndicator> indiceIndicators;

    private final AssetIndicator assetIndicator;

    private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    @Builder
    protected StrategyExecutor(String ruleConfig, String ruleScript, LocalDateTime dateTime, OrderBook orderBook, Balance balance, List<IndiceIndicator> indiceIndicators, AssetIndicator assetIndicator) {
        this.ruleConfig = ruleConfig;
        this.ruleScript = ruleScript;
        this.dateTime = dateTime;
        this.orderBook = orderBook;
        this.balance = balance;
        this.indiceIndicators = indiceIndicators.stream()
                .collect(Collectors.toMap(indiceIndicator ->
                        indiceIndicator.getIndiceId().name(), indiceIndicator -> indiceIndicator));
        this.assetIndicator = assetIndicator;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public BigDecimal execute() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(classLoader);
        Binding binding = new Binding();
        binding.setVariable("config", loadRuleConfigAsProperties(ruleConfig));
        binding.setVariable("log", log);
        binding.setVariable("dateTime", dateTime);
        binding.setVariable("orderBook", orderBook);
        binding.setVariable("balance", balance);
        binding.setVariable("indiceIndicators", indiceIndicators);
        binding.setVariable("assetIndicator", assetIndicator);
        GroovyShell groovyShell = new GroovyShell(groovyClassLoader, binding);
        if(ruleScript == null || ruleScript.isBlank()) {
            return null;
        }
        Object result = groovyShell.evaluate(ruleScript);
        if(result == null) {
            return null;
        }
        return new BigDecimal(result.toString());
    }

    private Properties loadRuleConfigAsProperties(String propertiesString) {
        Properties properties = new Properties();
        if (ruleConfig != null && !ruleConfig.isBlank()) {
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