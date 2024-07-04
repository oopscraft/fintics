package org.oopscraft.fintics.client.broker;

import java.time.ZoneId;

public abstract class UsBrokerClientDefinition implements BrokerClientDefinition {

    @Override
    public String getMarket() {
        return "US";
    }

    @Override
    public ZoneId getTimezone() {
        return ZoneId.of("America/New_York");
    }

}
