package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.BrokerEntity;
import org.oopscraft.fintics.dao.BrokerRepository;
import org.oopscraft.fintics.dao.BrokerSpecifications;
import org.oopscraft.fintics.model.Broker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrokerService {

    private final BrokerRepository brokerRepository;

    public Page<Broker> getBrokers(String brokerName, Pageable pageable) {
        Specification<BrokerEntity> specification = Specification.where(null);
        specification = specification.and(Optional.ofNullable(brokerName)
                                .map(BrokerSpecifications::containsBrokerName)
                                .orElse(null));

        Page<BrokerEntity> brokerEntityPage = brokerRepository.findAll(specification, pageable);
        List<Broker> brokers = brokerEntityPage.getContent().stream()
                .map(Broker::from)
                .toList();
        long total = brokerEntityPage.getTotalElements();
        return new PageImpl<>(brokers, pageable, total);
    }

    public Optional<Broker> getBroker(String brokerId) {
        return brokerRepository.findById(brokerId)
                .map(Broker::from);
    }

}
