package org.oopscraft.fintics.client.kis;

import org.oopscraft.fintics.client.Client;
import org.oopscraft.fintics.client.ClientDefinition;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.StringJoiner;

@Component
public class KisClientDefinition implements ClientDefinition {

    @Override
    public Class<? extends Client> getType() {
        return KisClient.class;
    }

    @Override
    public String getName() {
        return "한국투자증권 Kis API";
    }

    @Override
    public String getPropertiesTemplate() {
        StringJoiner template = new StringJoiner("\n");
        template.add("production=false");
        template.add("apiUrl=https://openapivts.koreainvestment.com:29443");
        template.add("appKey=[발급appkey]");
        template.add("appSecret=[발급appsecret]");
        template.add("accountNo=[계좌번호]");
        return template.toString();
    }

}
