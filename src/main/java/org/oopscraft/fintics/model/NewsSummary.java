package org.oopscraft.fintics.model;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetNewsSummary implements Serializable {

    private String id;

    private String name;

    private Long totalCount;

    private Instant maxDatetime;

    private Instant minDatetime;

    @Builder.Default
    private List<NewsStatistic> newsStatisticList = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NewsStatistic implements Serializable {
        private Instant date;
        private Long count;
    }

}
