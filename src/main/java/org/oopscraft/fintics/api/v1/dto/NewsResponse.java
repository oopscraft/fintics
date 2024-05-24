package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.News;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsResponse {

    private LocalDateTime dateTime;

    private String newsId;

    private String newsUrl;

    private String title;

    private News.Sentiment sentiment;

    private BigDecimal confidence;

    private String reason;

    public static NewsResponse from(News news) {
        return NewsResponse.builder()
                .dateTime(news.getDateTime())
                .newsId(news.getNewsId())
                .newsUrl(news.getNewsUrl())
                .title(news.getTitle())
                .sentiment(news.getSentiment())
                .confidence(news.getConfidence())
                .reason(news.getReason())
                .build();
    }

}
