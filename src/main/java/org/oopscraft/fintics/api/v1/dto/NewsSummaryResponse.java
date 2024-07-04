package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.NewsSummary;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetNewsSummaryResponse {

    private String id;

    private String name;

    private Long totalCount;

    private Instant maxDatetime;

    private Instant minDatetime;

    @Builder.Default
    @Setter
    private List<AssetNewsSummaryResponse.NewsStatisticResponse> newsStatistics = new ArrayList<>();

    public static AssetNewsSummaryResponse from(NewsSummary newsSummary) {
        return AssetNewsSummaryResponse.builder()
                .id(newsSummary.getId())
                .name(newsSummary.getName())
                .totalCount(newsSummary.getTotalCount())
                .maxDatetime(newsSummary.getMaxDatetime())
                .minDatetime(newsSummary.getMinDatetime())
                .newsStatistics(newsSummary.getNewsStatisticList().stream()
                        .map(AssetNewsSummaryResponse.NewsStatisticResponse::from)
                        .toList())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NewsStatisticResponse {

        private Instant date;
        private Long count;

        public static AssetNewsSummaryResponse.NewsStatisticResponse from(NewsSummary.NewsStatistic newsStatistic) {
            return AssetNewsSummaryResponse.NewsStatisticResponse.builder()
                    .date(newsStatistic.getDate())
                    .count(newsStatistic.getCount())
                    .build();
        }

    }

}
