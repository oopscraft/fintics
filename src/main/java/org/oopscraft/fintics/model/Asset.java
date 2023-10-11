package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class Asset {

    private String symbol;

    private String name;

    private AssetType type;

}
