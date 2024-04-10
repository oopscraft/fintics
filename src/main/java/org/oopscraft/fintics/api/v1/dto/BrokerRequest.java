package org.oopscraft.fintics.api.v1.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrokerRequest {

    private String brokerId;

    private String brokerName;

    private String brokerClientId;

    private String brokerClientProperties;

}
