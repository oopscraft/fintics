package org.oopscraft.fintics.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LinkFactory {

    public static List<Link> getLinks(Asset asset) {
        String market = asset.getMarket();
        String symbol = asset.getSymbol();
        List<Link> links = new ArrayList<>();
        switch(Optional.ofNullable(market).orElse("")) {
            case "US" -> {
                links.add(Link.of("Yahoo", "https://finance.yahoo.com/chart/" + symbol));
                links.add(Link.of("Finviz", "https://finviz.com/quote.ashx?t=" + symbol));
            }
            case "KR" -> {
                links.add(Link.of("Naver", "https://finance.naver.com/item/fchart.naver?code=" + symbol));
                links.add(Link.of("Alphasquare", "https://alphasquare.co.kr/home/market-summary?code=" + symbol));
            }
            case "UPBIT" -> {
                links.add(Link.of("UPBIT", "https://upbit.com/exchange?code=CRIX.UPBIT." + symbol));
            }
        }
        return links;
    }

}
