package org.oopscraft.fintics.client.broker.upbit;

import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.springframework.stereotype.Component;

import java.util.StringJoiner;

@Component
public class UpbitBrokerClientDefinition implements BrokerClientDefinition {

    @Override
    public String getBrokerId() {
        return "UPBIT";
    }

    @Override
    public String getBrokerName() {
        return "Upbit API";
    }

    @Override
    public Class<? extends BrokerClient> getClassType() {
        return UpbitBrokerClient.class;
    }

    @Override
    public String getConfigTemplate() {
        StringJoiner template = new StringJoiner("\n");
        template.add("accessKey=[Access Key]");
        template.add("secretKey=[Secret Key]");
        return template.toString();
    }

}
