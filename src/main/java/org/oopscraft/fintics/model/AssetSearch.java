package org.oopscraft.fintics.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetSearch {

    private String symbol;

    private String name;

    private AssetType type;

    private String country;

}
