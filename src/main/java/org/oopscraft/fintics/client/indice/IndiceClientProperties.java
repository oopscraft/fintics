package org.oopscraft.fintics.client.indice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@ConfigurationProperties(prefix = "fintics.indice-client")
@ConstructorBinding
@AllArgsConstructor
@Getter
public class IndiceClientProperties {

    private Class<? extends IndiceClient> className;

    private Map<String, String> properties;

    public Optional<String> getProperty(String name) {
        return Optional.ofNullable(properties.get(name));
    }

}
