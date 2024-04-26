package org.oopscraft.fintics.client.ohlcv;

import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Ohlcv;

import java.time.LocalDateTime;
import java.util.List;

public abstract class OhlcvClient {

    public abstract List<Ohlcv> getAssetOhlcvs(Asset asset, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo);

    public abstract List<Ohlcv> getIndiceOhlcvs(Indice indice, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo);

}
