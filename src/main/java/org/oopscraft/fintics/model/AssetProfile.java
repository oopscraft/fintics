package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetProfile extends Profile<Asset> {

    @Override
    public String getProfileName() {
        return getTarget().getAssetName();
    }

}
