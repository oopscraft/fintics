package org.oopscraft.fintics.client.indice;

import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Ohlcv;

import java.time.LocalDateTime;
import java.util.List;

public abstract class IndiceClient {

    public abstract List<Ohlcv> getMinuteOhlcvs(Indice.Id indiceId, LocalDateTime dateTime);

    public abstract List<Ohlcv> getDailyOhlcvs(Indice.Id indiceId, LocalDateTime dateTime);

}
