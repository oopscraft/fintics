package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.StrategyEntity;
import org.oopscraft.fintics.dao.StrategyRepository;
import org.oopscraft.fintics.dao.StrategySpecifications;
import org.oopscraft.fintics.model.Strategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final StrategyRepository strategyRepository;

    public Page<Strategy> getStrategies(String strategyName, Pageable pageable) {
        // where
        Specification<StrategyEntity> specification = Specification.where(null);
        specification = specification
                .and(Optional.ofNullable(strategyName)
                        .map(StrategySpecifications::containsRuleName)
                        .orElse(null));

        // find
        Page<StrategyEntity> strategyEntityPage = strategyRepository.findAll(specification, pageable);
        List<Strategy> strategies = strategyEntityPage.getContent().stream()
                .map(Strategy::from)
                .toList();
        long total = strategyEntityPage.getTotalElements();
        return new PageImpl<>(strategies, pageable, total);
    }

    public Optional<Strategy> getStrategy(String strategyId) {
        return strategyRepository.findById(strategyId)
                .map(Strategy::from);

    }

}
