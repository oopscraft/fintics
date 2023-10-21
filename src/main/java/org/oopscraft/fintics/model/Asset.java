package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.AssetEntity;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Asset {

    private String symbol;

    private String name;

    private AssetType type;

    private LocalDateTime collectedAt;

    public static Asset from(AssetEntity assetEntity) {
        return Asset.builder()
                .symbol(assetEntity.getSymbol())
                .name(assetEntity.getName())
                .type(assetEntity.getType())
                .collectedAt(assetEntity.getCollectedAt())
                .build();
    }

}
