package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.fintics.model.AssetNews;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fintics_asset_news")
@IdClass(AssetNewsEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetNewsEntity extends BaseEntity {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pk implements Serializable {
        private String assetId;
        private Instant datetime;
        private String newsId;
    }

    @Id
    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Id
    @Column(name = "datetime")
    private Instant datetime;

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
    private AssetNews.Sentiment sentiment;

    @Column(name = "confidence", scale = 2)
    private BigDecimal confidence;

    @Column(name = "reason")
    @Lob
    private String reason;

}
