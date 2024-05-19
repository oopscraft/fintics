package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.NewsSummary;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsSummaryResponse {

    private String id;

    private String name;

    private Long totalCount;

    private LocalDateTime maxDateTime;

    private LocalDateTime minDateTime;

    public static NewsSummaryResponse from(NewsSummary newsSummary) {
        return NewsSummaryResponse.builder()
                .id(newsSummary.getId())
                .name(newsSummary.getName())
                .totalCount(newsSummary.getTotalCount())
                .maxDateTime(newsSummary.getMaxDateTime())
                .minDateTime(newsSummary.getMinDateTime())
                .build();
    }

}
