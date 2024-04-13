package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.Builder;
import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.model.*;
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

    private final Strategy strategy;

    private final String variables;

    private final LocalDateTime dateTime;

    private final OrderBook orderBook;

    private final Balance balance;

    private final Map<String, IndiceProfile> indiceProfiles;

    private final AssetProfile assetProfile;

    private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    @Builder
    protected StrategyExecutor(Strategy strategy, String variables, LocalDateTime dateTime, OrderBook orderBook, Balance balance, List<IndiceProfile> indiceProfiles, AssetProfile assetProfile) {
        this.strategy = strategy;
        this.variables = variables;
        this.dateTime = dateTime;
        this.orderBook = orderBook;
        this.balance = balance;
        this.indiceProfiles = indiceProfiles.stream()
                .collect(Collectors.toMap(indiceProfile -> indiceProfile.getTarget().getIndiceId().name(), indiceProfile -> indiceProfile));
        this.assetProfile = assetProfile;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public BigDecimal execute() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(classLoader);
        Binding binding = new Binding();
        binding.setVariable("variables", loadRuleConfigAsProperties(variables));
        binding.setVariable("log", log);
        binding.setVariable("dateTime", dateTime);
        binding.setVariable("orderBook", orderBook);
        binding.setVariable("balance", balance);
        binding.setVariable("indiceProfiles", indiceProfiles);
        binding.setVariable("assetProfile", assetProfile);
        GroovyShell groovyShell = new GroovyShell(groovyClassLoader, binding);
        Object result = groovyShell.evaluate(strategy.getScript());
        if(result == null) {
            return null;
        }
        return new BigDecimal(result.toString());
    }

    private Properties loadRuleConfigAsProperties(String propertiesString) {
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