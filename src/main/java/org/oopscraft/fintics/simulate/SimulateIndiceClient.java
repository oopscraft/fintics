package org.oopscraft.fintics.simulate;

import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.model.IndiceId;
import org.oopscraft.fintics.model.Ohlcv;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SimulateIndiceClient extends IndiceClient {

    @Override
    public List<Ohlcv> getMinuteOhlcvs(IndiceId symbol, LocalDateTime dateTime) {
        return new ArrayList<>();
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(IndiceId symbol, LocalDateTime dateTime) {
        return new ArrayList<>();
    }

}
