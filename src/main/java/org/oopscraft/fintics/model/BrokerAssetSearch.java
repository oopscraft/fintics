package org.oopscraft.fintics.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrokerAssetSearch {

    private String assetId;

    private String assetName;

    private BrokerAsset.Type type;

}
