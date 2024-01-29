package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.oopscraft.fintics.model.Broker;

@Builder
@Getter
public class BrokerResponse {

    private String brokerId;

    private String brokerName;

    private Class<? extends BrokerClient> classType;

    private String configTemplate;

    public static BrokerResponse from(Broker broker) {
        return BrokerResponse.builder()
                .brokerId(broker.getBrokerId())
                .brokerName(broker.getBrokerName())
                .classType(broker.getClassType())
                .configTemplate(broker.getConfigTemplate())
                .build();
    }

}
