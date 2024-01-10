package org.oopscraft.fintics.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class Asset {

    private String assetId;

    private String assetName;

}
