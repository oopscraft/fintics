package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.fintics.model.News;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * news entity
 */
@Entity
@Table(name = "fintics_news")
@IdClass(NewsEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsEntity extends BaseEntity {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pk implements Serializable {
        private String assetId;
        private LocalDateTime dateTime;
        private String newsId;
    }

    @Id
    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Id
    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Id
    @Column(name = "news_id", length = 32)
    private String newsId;

    @Column(name = "news_url")
    @Lob
    private String newsUrl;

    @Column(name = "title")
    @Lob
    private String title;

    @Column(name = "sentiment", length = 16)
    private News.Sentiment sentiment;

    @Column(name = "confidence", scale = 2)
    private BigDecimal confidence;

    @Column(name = "reason")
    @Lob
    private String reason;

}
