package org.oopscraft.fintics.client.asset;

import lombok.Getter;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.AssetOhlcv;

import java.time.Instant;
import java.util.List;

public abstract class AssetOhlcvClient {

    @Getter
    private final AssetOhlcvClientProperties ohlcvClientProperties;

    public AssetOhlcvClient(AssetOhlcvClientProperties ohlcvClientProperties) {
        this.ohlcvClientProperties = ohlcvClientProperties;
    }

    public abstract boolean isSupported(Asset asset);

    public abstract List<AssetOhlcv> getOhlcvs(Asset asset, AssetOhlcv.Type type, Instant datetimeFrom, Instant datetimeTo);

}
