package org.oopscraft.fintics.client.broker.kis;

import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.StringJoiner;

@Component
public class KisUsBrokerClientDefinition implements BrokerClientDefinition {

    @Override
    public String getBrokerClientId() {
        return "KIS_US";
    }

    @Override
    public String getBrokerClientName() {
        return "Korea Investment Kis API (US Market)";
    }

    @Override
    public Class<? extends BrokerClient> getClassType() {
        return KisUsBrokerClient.class;
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

    @Override
    public String getMarket() {
        return "US";
    }

    @Override
    public ZoneId getTimezone() {
        return ZoneId.of("America/New_York");
    }

}
