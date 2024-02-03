package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Link;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LinkResponse {

    private String name;

    private String url;

    public static LinkResponse from(Link link) {
        return LinkResponse.builder()
                .name(link.getName())
                .url(link.getUrl())
                .build();
    }

    public static List<LinkResponse> from(List<Link> links) {
        return links.stream()
                .map(LinkResponse::from)
                .collect(Collectors.toList());
    }

}
