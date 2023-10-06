package org.oopscraft.fintics.client.kis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix="fintics.client.kis")
@ConstructorBinding
@AllArgsConstructor
@Getter
@Validated
public class KisClientProperties {

    private boolean production;

    private String apiUrl;

    private String appKey;

    private String appSecret;

    private String accountNo;

}
