package org.oopscraft.fintics.client.indice;

import org.oopscraft.fintics.model.IndiceId;
import org.oopscraft.fintics.model.Ohlcv;

import java.time.LocalDateTime;
import java.util.List;

public abstract class IndiceClient {

    public abstract List<Ohlcv> getMinuteOhlcvs(IndiceId indiceId, LocalDateTime dateTime);

    public abstract List<Ohlcv> getDailyOhlcvs(IndiceId indiceId, LocalDateTime dateTime);

}
