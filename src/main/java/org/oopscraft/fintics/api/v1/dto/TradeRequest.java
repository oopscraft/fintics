package org.oopscraft.fintics.api.v1.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeRequest {

    private String tradeId;

    private String name;

    private boolean enabled;

    private Integer interval;

    private String clientType;

    private String clientProperties;

    private String buyRule;

    private String sellRule;

    @Builder.Default
    private List<TradeAssetRequest> tradeAssets = new ArrayList<>();

}