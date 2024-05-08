package org.oopscraft.fintics.client.indice;

import lombok.Getter;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Ohlcv;

import java.time.LocalDateTime;
import java.util.List;

public abstract class IndiceClient {

    @Getter
    private final IndiceClientProperties indiceClientProperties;

    protected IndiceClient(IndiceClientProperties indiceClientProperties) {
        this.indiceClientProperties = indiceClientProperties;
    }

    public abstract List<Ohlcv> getMinuteOhlcvs(Indice.Id indiceId, LocalDateTime dateTime);

    public abstract List<Ohlcv> getDailyOhlcvs(Indice.Id indiceId, LocalDateTime dateTime);

}
