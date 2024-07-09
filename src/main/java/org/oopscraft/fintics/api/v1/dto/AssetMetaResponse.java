package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.AssetMeta;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetMetaResponse {

    private String assetId;

    private String name;

    private String value;

    private Instant dateTime;

    private Integer sort;

    public static AssetMetaResponse from(AssetMeta assetMeta) {
        return AssetMetaResponse.builder()
                .assetId(assetMeta.getAssetId())
                .name(assetMeta.getName())
                .value(assetMeta.getValue())
                .dateTime(assetMeta.getDateTime())
                .sort(assetMeta.getSort())
                .build();
    }

    public static List<AssetMetaResponse> from(List<AssetMeta> assetMetas) {
        return assetMetas.stream()
                .map(AssetMetaResponse::from)
                .collect(Collectors.toList());
    }

}
