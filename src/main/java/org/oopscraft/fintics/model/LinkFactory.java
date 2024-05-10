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
                links.add(Link.of("Yahoo", "https://finance.yahoo.com/quote/" + symbol));
                links.add(Link.of("Naver", "https://m.stock.naver.com/worldstock/stock/" + symbol + ".O"));
            }
            case "KR" -> {
                links.add(Link.of("Naver", "https://finance.naver.com/item/main.naver?code=" + symbol));
            }
            case "UPBIT" -> {
                links.add(Link.of("UPBIT", "https://upbit.com/exchange?code=CRIX.UPBIT." + symbol));
            }
        }
        return links;
    }

}
