package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Asset;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AssetResponse {

    private String assetId;

    private String assetName;

    @Builder.Default
    private List<LinkResponse> links = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class LinkResponse {
        private String name;
        private String url;
        public static LinkResponse from(Asset.Link assetLink) {
            return LinkResponse.builder()
                    .name(assetLink.getName())
                    .url(assetLink.getUrl())
                    .build();
        }
        public static List<LinkResponse> from(List<Asset.Link> assetLinks) {
            return assetLinks.stream()
                    .map(LinkResponse::from)
                    .toList();
        }
    }

}
