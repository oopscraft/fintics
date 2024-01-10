package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.OhlcvType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetOhlcvRepository extends JpaRepository<AssetOhlcvEntity, AssetOhlcvEntity.Pk> {

    @Query("select max(a.dateTime) from AssetOhlcvEntity a " +
            " where a.tradeClientId = :tradeClientId" +
            " and a.id = :id" +
            " and a.ohlcvType = :ohlcvType")
    Optional<LocalDateTime> findMaxDateTimeBySymbolAndOhlcvType(
            @Param("tradeClientId")String tradeClientId,
            @Param("id")String id,
            @Param("ohlcvType")OhlcvType ohlcvType
    );

    @Query("select a from AssetOhlcvEntity a " +
            " where a.tradeClientId = :tradeClientId" +
            " and a.id = :id" +
            " and a.ohlcvType = :ohlcvType" +
            " and a.dateTime between :dateTimeFrom and :dateTimeTo" +
            " order by a.dateTime desc")
    List<AssetOhlcvEntity> findAllBySymbolAndOhlcvType(
            @Param("tradeClientId")String tradeClientId,
            @Param("id")String id,
            @Param("ohlcvType")OhlcvType ohlcvType,
            @Param("dateTimeFrom")LocalDateTime dateTimeFrom,
            @Param("dateTimeTo")LocalDateTime dateTimeTo,
            Pageable pageable
    );

}
