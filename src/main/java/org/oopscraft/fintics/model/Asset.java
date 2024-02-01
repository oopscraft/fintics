package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;

import javax.persistence.Converter;
import java.math.BigDecimal;
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
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
