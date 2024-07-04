package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.NewsSummary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsSummaryResponse {

    private String assetId;

    private String assetName;

    private Long totalCount;

    private LocalDateTime maxDateTime;

    private LocalDateTime minDateTime;

    @Builder.Default
    @Setter
    private List<NewsSummaryResponse.NewsStatisticResponse> newsStatistics = new ArrayList<>();

    public static NewsSummaryResponse from(NewsSummary newsSummary) {
        return NewsSummaryResponse.builder()
                .assetId(newsSummary.getAssetId())
                .assetName(newsSummary.getAssetName())
                .totalCount(newsSummary.getTotalCount())
                .maxDateTime(newsSummary.getMaxDateTime())
                .minDateTime(newsSummary.getMinDateTime())
                .newsStatistics(newsSummary.getNewsStatisticList().stream()
                        .map(NewsSummaryResponse.NewsStatisticResponse::from)
                        .toList())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NewsStatisticResponse {

        private LocalDate date;

        private Long count;

        public static NewsSummaryResponse.NewsStatisticResponse from(NewsSummary.NewsStatistic newsStatistic) {
            return NewsSummaryResponse.NewsStatisticResponse.builder()
                    .date(newsStatistic.getDate())
                    .count(newsStatistic.getCount())
                    .build();
        }

    }

}
