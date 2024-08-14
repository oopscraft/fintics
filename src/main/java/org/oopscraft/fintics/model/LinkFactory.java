package org.oopscraft.fintics.model;

import java.util.*;

public class LinkFactory {

    public static List<Link> getLinks(Asset asset) {
        List<Link> links = new ArrayList<>();
        String symbol = asset.getSymbol();
        String market = Optional.ofNullable(asset.getMarket()).orElse("");
        switch (market) {
            case "US" -> links.addAll(getUsLinks(asset));
            case "KR" -> links.addAll(getKrLinks(asset));
        }
        return links;
    }

    static List<Link> getUsLinks(Asset asset) {
        List<Link> links = new ArrayList<>();
        String symbol = asset.getSymbol();
        String type = Optional.ofNullable(asset.getType()).orElse("");
        String exchange = Optional.ofNullable(asset.getExchange()).orElse("");
        // nasdaq
        switch (type) {
            case "STOCK" -> links.add(Link.of("Nasdaq", String.format("https://www.nasdaq.com/market-activity/stocks/%s", symbol)));
            case "ETF" -> links.add(Link.of("Nasdaq", String.format("https://www.nasdaq.com/market-activity/etf/%s", symbol)));
        }
        // yahoo
        links.add(Link.of("Yahoo", String.format("https://finance.yahoo.com/quote/%s", symbol)));
        // finviz
        links.add(Link.of("Finviz", String.format("https://finviz.com/quote.ashx?t=%s", symbol)));
        // seekingalpha
        links.add(Link.of("Seekingalpha", String.format("https://seekingalpha.com/symbol/%s", symbol)));
        // morningstar
        switch (exchange) {
            case "XASE" -> links.add(Link.of("Morningstar", String.format("https://www.morningstar.com/%ss/arcx/%s/quote", type.toLowerCase(), symbol.toLowerCase())));
            default -> links.add(Link.of("Morningstar", String.format("https://www.morningstar.com/%ss/%s/%s/quote", type.toLowerCase(), exchange.toLowerCase(), symbol.toLowerCase())));
        }
        // etf.com
        if (Objects.equals(type, "ETF")) {
            links.add(Link.of("etf.com", String.format("https://etf.com/%s", symbol)));
        }
        // return
        return links;
    }

    static List<Link> getKrLinks(Asset asset) {
        List<Link> links = new ArrayList<>();
        // naver
        links.add(Link.of("Naver", String.format("https://finance.naver.com/item/main.naver?code=%s", asset.getSymbol())));
        // alphasquare
        links.add(Link.of("Alphasquare", String.format("https://alphasquare.co.kr/home/market-summary?code=%s", asset.getSymbol())));
        // etfcheck
        if (Objects.equals(asset.getType(),"ETF")) {
            links.add(Link.of("ETFCheck", String.format("https://www.etfcheck.co.kr/mobile/etpitem/%s", asset.getSymbol())));
        }
        // return
        return links;
    }

}
