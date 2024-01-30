package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public class Asset {

    private String assetId;

    private String assetName;

    @Builder.Default
    private List<Link> links = new ArrayList<>();

    @Builder
    @Getter
    public static class Link {
        private String name;
        private String url;
        public static Link of(String name, String url) {
            return Link.builder()
                    .name(name)
                    .url(url)
                    .build();
        }
    }

}
