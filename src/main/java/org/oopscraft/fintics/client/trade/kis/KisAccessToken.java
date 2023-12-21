package org.oopscraft.fintics.client.trade.kis;

import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDateTime;

@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
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
        return LocalDateTime.now().isAfter(expireDateTime);
    }

}
