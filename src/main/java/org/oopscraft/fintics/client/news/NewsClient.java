package org.oopscraft.fintics.client.news;

import lombok.Getter;
import org.oopscraft.fintics.client.ohlcv.OhlcvClientProperties;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.News;

import java.util.List;

public abstract class NewsClient {

    @Getter
    private final NewsClientProperties newsClientProperties;

    public NewsClient(NewsClientProperties newsClientProperties) {
        this.newsClientProperties = newsClientProperties;
    }

    public abstract List<News> getAssetNewses(Asset asset);

    public abstract List<News> getIndiceNewses(Indice indice);

}