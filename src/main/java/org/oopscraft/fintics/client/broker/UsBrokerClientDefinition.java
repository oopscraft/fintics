package org.oopscraft.fintics.client.broker;

public abstract class UsBrokerClientDefinition implements BrokerClientDefinition {

    @Override
    public String getMarket() {
        return "US";
    }

}
