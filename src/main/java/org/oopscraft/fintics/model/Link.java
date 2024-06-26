package org.oopscraft.fintics.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Link {

    private String name;

    private String url;

    public static Link of(String name, String url) {
        return Link.builder()
                .name(name)
                .url(url)
                .build();
    }

}
