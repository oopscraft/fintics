package org.oopscraft.fintics.client.trade.kis;

import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientDefinition;
import org.springframework.stereotype.Component;

import java.util.StringJoiner;

@Component
public class KisTradeClientDefinition implements TradeClientDefinition {

    @Override
    public String getId() {
        return "KIS";
    }

    @Override
    public String getName() {
        return "한국투자증권 Kis API";
    }

    @Override
    public Class<? extends TradeClient> getType() {
        return KisTradeClient.class;
    }

    @Override
    public String getPropertiesTemplate() {
        StringJoiner template = new StringJoiner("\n");
        template.add("production=false");
        template.add("apiUrl=https://openapivts.koreainvestment.com:29443");
        template.add("appKey=[Application Key]");
        template.add("appSecret=[Application Secret]");
        template.add("accountNo=[Account Number]");
        return template.toString();
    }

}
