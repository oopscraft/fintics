package org.oopscraft.fintics.dao;

import org.springframework.data.jpa.domain.Specification;

public class BrokerSpecifications {

    public static Specification<BrokerEntity> containsBrokerName(String brokerName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(BrokerEntity_.BROKER_NAME), '%' + brokerName + '%');
    }

}
