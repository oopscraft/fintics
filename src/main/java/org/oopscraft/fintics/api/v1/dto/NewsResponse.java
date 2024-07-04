package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.News;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetNewsResponse {

    private Instant datetime;

    private String newsId;

    private String newsUrl;

    private String title;

    private News.Sentiment sentiment;

    private BigDecimal confidence;

    private String reason;

    public static AssetNewsResponse from(News assetNews) {
        return AssetNewsResponse.builder()
                .datetime(assetNews.getDatetime())
                .newsId(assetNews.getNewsId())
                .newsUrl(assetNews.getNewsUrl())
                .title(assetNews.getTitle())
                .sentiment(assetNews.getSentiment())
                .confidence(assetNews.getConfidence())
                .reason(assetNews.getReason())
                .build();
    }

}
