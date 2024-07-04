package org.oopscraft.fintics.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetFinancialRepository extends JpaRepository<AssetFinancialEntity, String>, JpaSpecificationExecutor<AssetFinancialEntity> {

}
