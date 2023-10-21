package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.dao.AssetRepository;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetType;

import java.time.LocalDateTime;

@Builder
@Getter
public class AssetResponse {

    private String symbol;

    private String name;

    private AssetType type;

    private LocalDateTime collectedAt;

    public static AssetResponse from(Asset asset) {
        return AssetResponse.builder()
                .symbol(asset.getSymbol())
                .name(asset.getName())
                .type(asset.getType())
                .collectedAt(asset.getCollectedAt())
                .build();
    }

}
