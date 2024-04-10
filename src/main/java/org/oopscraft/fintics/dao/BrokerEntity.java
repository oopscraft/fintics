package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;

import javax.persistence.*;

@Entity
@Table(name = "fintics_broker")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrokerEntity extends BaseEntity {

    @Id
    @Column(name = "broker_id", length = 32)
    private String brokerId;

    @Column(name = "broker_name")
    private String brokerName;

    @Column(name = "broker_client_id", length = 32)
    private String brokerClientId;

    @Column(name = "broker_client_config")
    @Lob
    private String brokerClientConfig;

}
