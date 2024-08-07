package org.oopscraft.fintics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.oopscraft.arch4j.web.security.SecurityPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * fintics properties
 */
@ConfigurationProperties(prefix = "fintics")
@ConstructorBinding
@AllArgsConstructor
@Getter
public final class FinticsProperties {

    private final String systemAlarmId;

    private final String aiApiUrl;

    private final Integer dataRetentionMonths;

}
