package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.AssetType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAssetRequest {

    private String tradeId;

    private String symbol;

    private String name;

    private AssetType type;

    private boolean enabled;

    private BigDecimal holdRatio;

}
