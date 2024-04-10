package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.core.data.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.dao.StrategyEntity;
import org.oopscraft.fintics.dao.StrategyRepository;
import org.oopscraft.fintics.dao.StrategySpecifications;
import org.oopscraft.fintics.model.Strategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final StrategyRepository strategyRepository;

    public Page<Strategy> getStrategies(String strategyName, Pageable pageable) {
        Specification<StrategyEntity> specification = Specification.where(null);
        specification = specification
                .and(Optional.ofNullable(strategyName)
                        .map(StrategySpecifications::containsRuleName)
                        .orElse(null));
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

    @Transactional
    public Strategy saveStrategy(Strategy strategy) {
        StrategyEntity strategyEntity;
        if (strategy.getStrategyId() == null) {
            strategyEntity = StrategyEntity.builder()
                    .strategyId(IdGenerator.uuid())
                    .build();
        } else {
            strategyEntity = strategyRepository.findById(strategy.getStrategyId()).orElseThrow();
        }
        strategyEntity.setStrategyName(strategy.getStrategyName());
        strategyEntity.setLanguage(strategy.getLanguage());
        if (strategy.getVariables() != null) {
            strategyEntity.setVariables(PbePropertiesUtil.encode(strategy.getVariables()));
        }
        strategyEntity.setScript(strategy.getScript());
        StrategyEntity savedStrategyEntity = strategyRepository.saveAndFlush(strategyEntity);
        return Strategy.from(savedStrategyEntity);
    }

    @Transactional
    public void deleteStrategy(String strategyId) {
        StrategyEntity strategyEntity = strategyRepository.findById(strategyId).orElseThrow();
        strategyRepository.delete(strategyEntity);
        strategyRepository.flush();
    }

}
