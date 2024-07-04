package org.oopscraft.fintics.client.broker;

import java.time.ZoneId;

public abstract class KrBrokerClientDefinition implements BrokerClientDefinition {

    @Override
    public String getMarket() {
        return "KR";
    }

    @Override
    public ZoneId getTimezone() {
        return ZoneId.of("Asia/Seoul");
    }

}
