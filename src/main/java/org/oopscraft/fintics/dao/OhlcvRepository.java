package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.AssetOhlcv;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AssetOhlcvRepository extends JpaRepository<AssetOhlcvEntity, AssetOhlcvEntity.Pk>, JpaSpecificationExecutor<AssetOhlcvEntity> {

    @Query("select a from AssetOhlcvEntity a " +
            " where a.assetId = :assetId" +
            " and a.type = :type" +
            " and a.datetime between :datetimeFrom and :datetimeTo" +
            " order by a.datetime desc")
    List<AssetOhlcvEntity> findAllByAssetIdAndType(
            @Param("assetId") String assetId,
            @Param("type") AssetOhlcv.Type type,
            @Param("datetimeFrom") Instant datetimeFrom,
            @Param("datetimeTo") Instant datetimeTo,
            Pageable pageable
    );

}
