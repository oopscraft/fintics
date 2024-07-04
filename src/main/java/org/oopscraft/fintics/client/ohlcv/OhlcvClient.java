package org.oopscraft.fintics.client.ohlcv;

import lombok.Getter;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Ohlcv;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public abstract class OhlcvClient {

    @Getter
    private final OhlcvClientProperties ohlcvClientProperties;

    public OhlcvClient(OhlcvClientProperties ohlcvClientProperties) {
        this.ohlcvClientProperties = ohlcvClientProperties;
    }

    public abstract boolean isSupported(Asset asset);

    public abstract List<Ohlcv> getOhlcvs(Asset asset, Ohlcv.Type type, LocalDateTime datetimeFrom, LocalDateTime datetimeTo);

}
