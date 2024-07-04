package org.oopscraft.fintics.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AssetOhlcvSplitRepository extends JpaRepository<AssetOhlcvSplitEntity, AssetOhlcvSplitEntity.Pk>, JpaSpecificationExecutor<AssetOhlcvSplitEntity> {

    @Query("select a from AssetOhlcvSplitEntity a " +
            " where a.assetId = :assetId" +
            " and a.datetime between :datetimeFrom and :datetimeTo" +
            " order by a.datetime desc")
    List<AssetOhlcvSplitEntity> findAllByAssetId(
            @Param("assetId") String assetId,
            @Param("datetimeFrom") Instant datetimeFrom,
            @Param("datetimeTo") Instant datetimeTo
    );

}
