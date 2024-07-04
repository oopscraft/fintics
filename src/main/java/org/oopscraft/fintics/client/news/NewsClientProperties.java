package org.oopscraft.fintics.client.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Map;
import java.util.Optional;

@ConfigurationProperties(prefix = "fintics.asset-news-client")
@ConstructorBinding
@AllArgsConstructor
@Getter
@Builder
public class AssetNewsClientProperties {

    private final Class<? extends AssetNewsClient> className;

    private final Map<String, String> properties;

    public Optional<String> getProperty(String name) {
        return Optional.ofNullable(properties.get(name));
    }

}
