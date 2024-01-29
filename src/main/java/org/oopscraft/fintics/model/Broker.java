package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.api.v1.dto.BrokerResponse;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;

@Builder
@Getter
public class Broker {

    private String brokerId;

    private String brokerName;

    private Class<? extends BrokerClient> classType;

    private String configTemplate;

    public static Broker from(BrokerClientDefinition brokerClientDefinition) {
        return Broker.builder()
                .brokerId(brokerClientDefinition.getBrokerId())
                .brokerName(brokerClientDefinition.getBrokerName())
                .classType(brokerClientDefinition.getClassType())
                .configTemplate(brokerClientDefinition.getConfigTemplate())
                .build();
    }

}
