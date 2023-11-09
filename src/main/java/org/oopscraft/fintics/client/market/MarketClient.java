package org.oopscraft.fintics.client.market;

import org.oopscraft.fintics.model.MarketIndicator;

public interface MarketClient {

    MarketIndicator getNdxIndicator() throws InterruptedException;

    MarketIndicator getNdxFutureIndicator() throws InterruptedException;

    MarketIndicator getSpxIndicator() throws InterruptedException;

    MarketIndicator getSpxFutureIndicator() throws InterruptedException;

    MarketIndicator getDjiIndicator() throws InterruptedException;

    MarketIndicator getDjiFutureIndicator() throws InterruptedException;

    MarketIndicator getKospiIndicator() throws InterruptedException;

    MarketIndicator getUsdKrwIndicator() throws InterruptedException;

}
