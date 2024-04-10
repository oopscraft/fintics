package org.oopscraft.fintics.model.broker.upbit;

import org.oopscraft.fintics.model.broker.BrokerClient;
import org.oopscraft.fintics.model.broker.BrokerClientDefinition;
import org.springframework.stereotype.Component;

import java.util.StringJoiner;

@Component
public class UpbitBrokerClientDefinition implements BrokerClientDefinition {


    @Override
    public String getBrokerClientId() {
        return "UPBIT";
    }

    @Override
    public String getBrokerClientName() {
        return "Upbit API";
    }

    @Override
    public Class<? extends BrokerClient> getClassType() {
        return UpbitBrokerClient.class;
    }

    @Override
    public String getPropertiesTemplate() {
        StringJoiner template = new StringJoiner("\n");
        template.add("accessKey=[Access Key]");
        template.add("secretKey=[Secret Key]");
        return template.toString();
    }

    @Override
    public String getExchangeId() {
        return "UPBIT";
    }

}
