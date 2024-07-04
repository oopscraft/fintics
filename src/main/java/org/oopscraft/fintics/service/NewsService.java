package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.NewsRepository;
import org.oopscraft.fintics.model.News;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * news service
 */
@Component
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;

    /**
     * returns list of news
     * @param assetId asset id
     * @param dateTimeFrom date time from
     * @param dateTimeTo date time to
     * @param pageable pageable info
     * @return list of news
     */
    public List<News> getNewses(String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        return newsRepository.findAllByAssetId(assetId, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(News::from)
                .toList();
    }

}
