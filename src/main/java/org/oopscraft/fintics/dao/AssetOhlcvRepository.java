package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetOhlcvRepository extends JpaRepository<AssetOhlcvEntity, AssetOhlcvEntity.Pk>, JpaSpecificationExecutor<AssetOhlcvEntity> {

    @Query("select a from AssetOhlcvEntity a " +
            " where a.assetId = :assetId" +
            " and a.type = :type" +
            " and a.dateTime between :dateTimeFrom and :dateTimeTo" +
            " order by a.dateTime desc")
    List<AssetOhlcvEntity> findAllByAssetIdAndType(
            @Param("assetId") String assetId,
            @Param("type") Ohlcv.Type type,
            @Param("dateTimeFrom") LocalDateTime dateTimeFrom,
            @Param("dateTimeTo") LocalDateTime dateTimeTo,
            Pageable pageable
    );

}
