package org.oopscraft.fintics.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyRepository extends JpaRepository<StrategyEntity, String>, JpaSpecificationExecutor<StrategyEntity> {

}
