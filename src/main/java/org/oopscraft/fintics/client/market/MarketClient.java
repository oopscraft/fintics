package org.oopscraft.fintics.client.market;

import org.oopscraft.fintics.model.MarketIndicator;

import java.util.List;

public interface MarketClient {

    MarketIndicator getNdxIndicator();

    MarketIndicator getNdxFutureIndicator();

    MarketIndicator getSpxIndicator();

    MarketIndicator getSpxFutureIndicator();

    MarketIndicator getDjiIndicator();

    MarketIndicator getDjiFutureIndicator();


}
