package org.oopscraft.fintics.client.indice;

import org.oopscraft.fintics.model.IndiceSymbol;
import org.oopscraft.fintics.model.Ohlcv;

import java.util.List;

public abstract class IndiceClient {

    public abstract List<Ohlcv> getMinuteOhlcvs(IndiceSymbol symbol);

    public abstract List<Ohlcv> getDailyOhlcvs(IndiceSymbol symbol);

}
