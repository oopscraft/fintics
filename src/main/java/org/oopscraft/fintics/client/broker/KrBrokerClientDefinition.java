package org.oopscraft.fintics.client.broker;

public abstract class KrBrokerClientDefinition implements BrokerClientDefinition {

    @Override
    public String getMarket() {
        return "KR";
    }

}
