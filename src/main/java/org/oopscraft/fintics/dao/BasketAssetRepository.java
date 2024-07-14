package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.BasketSearch;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BasketAssetRepository extends JpaRepository<BasketAssetEntity, BasketAssetEntity.Pk>, JpaSpecificationExecutor<BasketAssetEntity> {

}
