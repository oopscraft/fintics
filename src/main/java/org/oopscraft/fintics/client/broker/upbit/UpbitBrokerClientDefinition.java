package org.oopscraft.fintics.client.broker.upbit;

import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.oopscraft.fintics.model.Asset;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
    public List<Asset.Link> getAssetLinks(Asset asset) {
        return new ArrayList<>() {{
            add(Asset.Link.of("UPBIT", "https://upbit.com/exchange?code=CRIX.UPBIT." + asset.getAssetId()));
        }};
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
