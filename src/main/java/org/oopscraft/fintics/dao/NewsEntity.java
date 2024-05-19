package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.fintics.model.News;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsEntity extends BaseEntity {

    @Id
    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Id
    @Column(name = "news_id", length = 32)
    private String newsId;

    @Column(name = "new_url")
    @Lob
    private String newsUrl;

    @Column(name = "title")
    private String title;

    @Column(name = "sentiment", length = 16)
    private News.Sentiment sentiment;

    @Column(name = "confidence", scale = 2)
    private BigDecimal confidence;

}
