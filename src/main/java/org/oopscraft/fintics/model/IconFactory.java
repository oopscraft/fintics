package org.oopscraft.fintics.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IconFactory {

    public static String getIcon(Asset asset) {
        String market = asset.getMarket();
        String type = asset.getType();
        String symbol = asset.getSymbol();
        String icon = "/static/image/icon-asset.svg";
        if ("STOCK".equals(type)) {
            switch(Optional.ofNullable(market).orElse("")) {
                case "US" -> {
                    icon = String.format("https://ssl.pstatic.net/imgstock/fn/real/logo/stock/Stock%s.O.svg", symbol);
                }
                case "KR" -> {
                    icon = String.format("https://ssl.pstatic.net/imgstock/fn/real/logo/stock/Stock%s.svg", symbol);
                }
            }
        }
        if ("ETF".equals(type)) {
            icon = "https://ssl.pstatic.net/imgstock/fn/real/logo/stock/StockCommonETF.svg";
        }

        // return
        return icon;
    }

}
