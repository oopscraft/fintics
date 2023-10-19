package org.oopscraft.fintics.client.kis;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class KisAccessToken {

    @EqualsAndHashCode.Include
    private String apiUrl;

    @EqualsAndHashCode.Include
    private String appKey;

    @EqualsAndHashCode.Include
    private String appSecret;

    private String accessToken;

    private LocalDateTime expireDateTime;

    public boolean isExpired() {
        return LocalDateTime.now()
                .isAfter(expireDateTime.minusHours(1));
    }

}
