package org.oopscraft.fintics.client.ohlcv;

import lombok.Getter;
import org.oopscraft.arch4j.core.common.support.RestTemplateBuilder;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * abstract class of ohlcv client
 */
public abstract class OhlcvClient {

    @Getter
    private final OhlcvClientProperties ohlcvClientProperties;

    /**
     * constructor
     * @param ohlcvClientProperties ohlcv client properties
     */
    public OhlcvClient(OhlcvClientProperties ohlcvClientProperties) {
        this.ohlcvClientProperties = ohlcvClientProperties;
    }

    /**
     * whether is supported
     * @param asset asset
     * @return support or not
     */
    public abstract boolean isSupported(Asset asset);

    public abstract List<Ohlcv> getOhlcvs(Asset asset, Ohlcv.Type type, LocalDateTime datetimeFrom, LocalDateTime datetimeTo);

    public RestTemplate getRestTemplate() {
        return RestTemplateBuilder.create()
                .retryCount(3)
                .build();
    }

}
