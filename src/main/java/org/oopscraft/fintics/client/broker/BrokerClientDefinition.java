package org.oopscraft.fintics.client.broker;

import org.springframework.beans.factory.Aware;

public interface BrokerClientDefinition extends Aware {

    String getBrokerId();

    String getBrokerName();

    Class<? extends BrokerClient> getClassType();

    String getConfigTemplate();

}
