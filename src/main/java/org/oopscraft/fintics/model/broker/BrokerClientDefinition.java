package org.oopscraft.fintics.model.broker;

import org.springframework.beans.factory.Aware;

public interface BrokerClientDefinition extends Aware {

    String getBrokerClientId();

    String getBrokerClientName();

    Class<? extends BrokerClient> getClassType();

    String getConfigTemplate();

    String getExchangeId();

}
