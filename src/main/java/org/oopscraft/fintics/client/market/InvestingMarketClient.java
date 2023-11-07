package org.oopscraft.fintics.client.market;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.model.MarketIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "fintics.client.market", name = "class-name", havingValue="org.oopscraft.fintics.client.market.InvestingMarketClient")
@RequiredArgsConstructor
@Slf4j
public class InvestingMarketClient implements MarketClient {

    @Override
    public MarketIndicator getNdxIndicator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MarketIndicator getNdxFutureIndicator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MarketIndicator getSpxIndicator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MarketIndicator getSpxFutureIndicator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MarketIndicator getDjiIndicator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MarketIndicator getDjiFutureIndicator() {
        throw new UnsupportedOperationException();
    }
}
