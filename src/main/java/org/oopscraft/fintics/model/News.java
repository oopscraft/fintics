package org.oopscraft.fintics.model;

import lombok.*;
import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.dao.NewsEntity;

import javax.persistence.Converter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * news
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class News {

    private String assetId;

    private LocalDateTime dateTime;

    private String newsId;

    private String newsUrl;

    private String title;

    private Sentiment sentiment;

    private BigDecimal confidence;

    private String reason;

    /**
     * news sentiment
     */
    public enum Sentiment {
        POSITIVE, NEUTRAL, NEGATIVE
    }

    /**
     * sentiment enum converter
     */
    @Converter(autoApply = true)
    public static class SentimentConverter extends AbstractEnumConverter<Sentiment> {}

    /**
     * creates news
     * @param newsEntity news entity
     * @return news
     */
    public static News from(NewsEntity newsEntity) {
        return News.builder()
                .assetId(newsEntity.getAssetId())
                .dateTime(newsEntity.getDateTime())
                .newsId(newsEntity.getNewsId())
                .newsUrl(newsEntity.getNewsUrl())
                .title(newsEntity.getTitle())
                .sentiment(newsEntity.getSentiment())
                .confidence(newsEntity.getConfidence())
                .reason(newsEntity.getReason())
                .build();
    }

}
