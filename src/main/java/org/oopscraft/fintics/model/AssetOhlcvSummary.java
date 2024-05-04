package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetOhlcvSummary extends OhlcvSummary {

    private String assetId;

    private String assetName;

    private Integer tradeCount;

}
