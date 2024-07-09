package org.oopscraft.fintics.model;

import lombok.*;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.dao.AssetMetaEntity;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetMeta {

    private String assetId;

    private String name;

    private String value;

    private Instant dateTime;

    private Integer sort;

    /**
     * from factory method
     * @param assetMetaEntity asset meta entity
     * @return asset meta
     */
    public static AssetMeta from(AssetMetaEntity assetMetaEntity) {
        return AssetMeta.builder()
                .assetId(assetMetaEntity.getAssetId())
                .name(assetMetaEntity.getName())
                .value(assetMetaEntity.getValue())
                .dateTime(assetMetaEntity.getDateTime())
                .sort(assetMetaEntity.getSort())
                .build();
    }

}
