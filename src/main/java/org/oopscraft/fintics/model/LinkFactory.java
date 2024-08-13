package org.oopscraft.fintics.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LinkFactory {

    public static List<Link> getLinks(Asset asset) {
        String market = asset.getMarket();
        String type = asset.getType();
        String symbol = asset.getSymbol();
        List<Link> links = new ArrayList<>();
        switch(Optional.ofNullable(market).orElse("")) {
            case "US" -> {
                // nasdaq
                switch (Optional.ofNullable(type).orElse("")) {
                    case "STOCK" -> links.add(Link.of("Nasdaq", String.format("https://www.nasdaq.com/market-activity/stocks/%s", symbol)));
                    case "ETF" -> links.add(Link.of("Nasdaq", String.format("https://www.nasdaq.com/market-activity/etf/%s", symbol)));
                }
                links.add(Link.of("Yahoo", String.format("https://finance.yahoo.com/quote/%s", symbol)));
                links.add(Link.of("Finviz", String.format("https://finviz.com/quote.ashx?t=%s", symbol)));
                // naver
                if (Objects.equals(type, "ETF")) {
                    links.add(Link.of("Naver", String.format("https://m.stock.naver.com/worldstock/etf/%s/total", symbol)));
                } else {
                    links.add(Link.of("Naver", String.format("https://m.stock.naver.com/worldstock/stock/%s.O/total", symbol)));
                }
            }
            case "KR" -> {
                links.add(Link.of("Naver", String.format("https://finance.naver.com/item/main.naver?code=%s", symbol)));
                links.add(Link.of("Alphasquare", String.format("https://alphasquare.co.kr/home/market-summary?code=%s", symbol)));
                // etf
                if (Objects.equals(type,"ETF")) {
                    links.add(Link.of("ETFCheck", String.format("https://www.etfcheck.co.kr/mobile/etpitem/%s", symbol)));
                }
            }
            case "UPBIT" -> {
                links.add(Link.of("UPBIT", String.format("https://upbit.com/exchange?code=CRIX.UPBIT.%s", symbol)));
            }
        }
        return links;
    }

}
