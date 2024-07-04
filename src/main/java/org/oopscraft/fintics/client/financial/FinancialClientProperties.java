package org.oopscraft.fintics.client.financial;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Map;
import java.util.Optional;

@ConfigurationProperties(prefix = "fintics.asset-financial-client")
@ConstructorBinding
@AllArgsConstructor
@Getter
public class FinancialClientProperties {

    private Class<? extends FinancialClient> className;

    private Map<String, String> properties;

    public Optional<String> getProperty(String name) {
        return Optional.ofNullable(properties.get(name));
    }

}
