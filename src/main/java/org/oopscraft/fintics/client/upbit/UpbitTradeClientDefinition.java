package org.oopscraft.fintics.client.upbit;

import org.oopscraft.fintics.client.TradeClient;
import org.oopscraft.fintics.client.TradeClientDefinition;
import org.springframework.stereotype.Component;

import java.util.StringJoiner;

@Component
public class UpbitTradeClientDefinition implements TradeClientDefinition {

    @Override
    public Class<? extends TradeClient> getType() {
        return UpbitTradeClient.class;
    }

    @Override
    public String getName() {
        return "업비트 API";
    }

    @Override
    public String getPropertiesTemplate() {
        StringJoiner template = new StringJoiner("\n");
        template.add("accessKey=[발급 accessKey]");
        template.add("secretKey=[발급 secretKey]");
        return template.toString();
    }

}
