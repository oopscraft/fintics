package org.oopscraft.fintics.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetMetaRepository extends JpaRepository<AssetMetaEntity, AssetMetaEntity.Pk>, JpaSpecificationExecutor<AssetMetaEntity> {

    @Query("select a from AssetMetaEntity a where a.assetId = :assetId order by sort")
    List<AssetMetaEntity> findAllByAssetId(@Param("assetId") String assetId);

}
