package org.oopscraft.fintics.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetSearch {

    private String assetId;

    private String assetName;

    private Asset.Type type;

}
