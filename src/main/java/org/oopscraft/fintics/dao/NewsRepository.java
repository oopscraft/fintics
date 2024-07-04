package org.oopscraft.fintics.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AssetNewsRepository extends JpaRepository<AssetNewsEntity, AssetNewsEntity.Pk> {

    @Query("select a from AssetNewsEntity a" +
            " where a.assetId = :assetId" +
            " and a.datetime between :datetimeFrom and :datetimeTo" +
            " order by a.datetime desc")
    List<AssetNewsEntity> findAllByAssetId(
           @Param("assetId") String assetId,
           @Param("datetimeFrom") Instant datetimeFrom,
           @Param("datetimeTo") Instant datetimeTo,
           Pageable pageable
    );

}
