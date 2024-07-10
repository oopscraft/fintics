package org.oopscraft.fintics.model;

import lombok.*;
import org.oopscraft.fintics.dao.BrokerEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Broker {

    private String brokerId;

    private String brokerName;

    private String brokerClientId;

    private String brokerClientProperties;

    /**
     * from factory method
     * @param brokerEntity broker entity
     * @return broker
     */
    public static Broker from(BrokerEntity brokerEntity) {
        return Broker.builder()
                .brokerId(brokerEntity.getBrokerId())
                .brokerName(brokerEntity.getBrokerName())
                .brokerClientId(brokerEntity.getBrokerClientId())
                .brokerClientProperties(brokerEntity.getBrokerClientProperties())
                .build();
    }

}
