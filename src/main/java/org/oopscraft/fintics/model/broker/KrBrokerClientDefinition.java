package org.oopscraft.fintics.model.broker;

public abstract class KrBrokerClientDefinition implements BrokerClientDefinition {

    @Override
    public String getExchangeId() {
        return "KR";
    }

}
