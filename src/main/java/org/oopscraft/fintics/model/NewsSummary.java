package org.oopscraft.fintics.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsSummary implements Serializable {

    private String id;

    private String name;

    private Long totalCount;

    private LocalDateTime maxDateTime;

    private LocalDateTime minDateTime;

    @Builder.Default
    private List<NewsStatistic> newsStatisticList = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NewsStatistic implements Serializable {
        private LocalDate date;
        private Long count;
    }

}
