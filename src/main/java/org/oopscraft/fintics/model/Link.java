package org.oopscraft.fintics.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
