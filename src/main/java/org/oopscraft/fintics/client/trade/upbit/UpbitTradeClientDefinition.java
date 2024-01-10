package org.oopscraft.fintics.client.trade.upbit;

import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientDefinition;
import org.springframework.stereotype.Component;

import java.util.StringJoiner;

@Component
public class UpbitTradeClientDefinition implements TradeClientDefinition {

    @Override
    public String getTradeClientId() {
        return "UPBIT";
    }

    @Override
    public String getTradeClientName() {
        return "업비트 API";
    }

    @Override
    public Class<? extends TradeClient> getClassType() {
        return UpbitTradeClient.class;
    }

    @Override
    public String getConfigTemplate() {
        StringJoiner template = new StringJoiner("\n");
        template.add("accessKey=[Access Key]");
        template.add("secretKey=[Secret Key]");
        return template.toString();
    }

}
