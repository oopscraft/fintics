package org.oopscraft.fintics.client.news;

import lombok.Getter;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.News;

import java.util.List;

public abstract class NewsClient {

    @Getter
    private final NewsClientProperties newsClientProperties;

    public NewsClient(NewsClientProperties newsClientProperties) {
        this.newsClientProperties = newsClientProperties;
    }

    public abstract List<News> getNewses(Asset asset);

}