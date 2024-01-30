package org.oopscraft.fintics.client.broker.kis;

import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.oopscraft.fintics.client.broker.KrxBrokerClientDefinition;
import org.springframework.stereotype.Component;

import java.util.StringJoiner;

@Component
public class KisBrokerClientDefinition extends KrxBrokerClientDefinition {

    @Override
    public String getBrokerId() {
        return "KIS";
    }

    @Override
    public String getBrokerName() {
        return "Korea Investment Kis API";
    }

    @Override
    public Class<? extends BrokerClient> getClassType() {
        return KisBrokerClient.class;
    }

    @Override
    public String getConfigTemplate() {
        StringJoiner template = new StringJoiner("\n");
        template.add("production=false");
        template.add("apiUrl=https://openapivts.koreainvestment.com:29443");
        template.add("appKey=[Application Key]");
        template.add("appSecret=[Application Secret]");
        template.add("accountNo=[Account Number]");
        return template.toString();
    }

}
