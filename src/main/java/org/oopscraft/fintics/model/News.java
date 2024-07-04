package org.oopscraft.fintics.model;

import lombok.*;
import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.dao.NewsEntity;

import javax.persistence.Converter;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetNews {

    private Instant datetime;

    private String newsId;

    private String newsUrl;

    private String title;

    private Sentiment sentiment;

    private BigDecimal confidence;

    private String reason;

    public enum Sentiment {
        POSITIVE, NEUTRAL, NEGATIVE
    }

    @Converter(autoApply = true)
    public static class SentimentConverter extends AbstractEnumConverter<Sentiment> {}

    public static AssetNews from(NewsEntity newsEntity) {
        return AssetNews.builder()
                .datetime(newsEntity.getDatetime())
                .newsId(newsEntity.getNewsId())
                .newsUrl(newsEntity.getNewsUrl())
                .title(newsEntity.getTitle())
                .sentiment(newsEntity.getSentiment())
                .confidence(newsEntity.getConfidence())
                .reason(newsEntity.getReason())
                .build();
    }

}
