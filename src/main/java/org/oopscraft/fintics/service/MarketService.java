package org.oopscraft.fintics.service;

import org.oopscraft.fintics.model.Market;
import org.oopscraft.fintics.model.MarketIndex;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MarketService {

    public Market getMarket() {
        Market market = Market.builder()
                .spx(getMarketIndexSpx())
                .spxFuture(getMarketIndexSpxFuture())
                .dji(getMarketIndexDji())
                .djiFuture(getMarketIndexDjiFuture())
                .ndx(getMarketIndexNdx())
                .ndxFuture(getMarketIndexNdxFuture())
                .build();
        return market;
    }

    private MarketIndex getMarketIndexSpx() {
        return null;
    }

    private MarketIndex getMarketIndexSpxFuture() {
        return null;
    }

    private MarketIndex getMarketIndexDji() {
        return null;
    }

    private MarketIndex getMarketIndexDjiFuture() {
        return null;
    }

    private MarketIndex getMarketIndexNdx() {
        return null;
    }

    private MarketIndex getMarketIndexNdxFuture() {
        return null;
    }



}
