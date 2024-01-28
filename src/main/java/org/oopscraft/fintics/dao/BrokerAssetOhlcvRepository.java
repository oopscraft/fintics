package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BrokerAssetOhlcvRepository extends JpaRepository<BrokerAssetOhlcvEntity, BrokerAssetOhlcvEntity.Pk> {

    @Query("select max(a.dateTime) from BrokerAssetOhlcvEntity a " +
            " where a.brokerId = :brokerId" +
            " and a.assetId = :assetId" +
            " and a.type = :type")
    Optional<LocalDateTime> findMaxDateTimeByBrokerIdAndAssetIdAndType(
            @Param("brokerId")String brokerId,
            @Param("assetId")String assetId,
            @Param("type") Ohlcv.Type type
    );

    @Query("select a from BrokerAssetOhlcvEntity a " +
            " where a.brokerId = :brokerId" +
            " and a.assetId = :assetId" +
            " and a.type = :type" +
            " and a.dateTime between :dateTimeFrom and :dateTimeTo" +
            " order by a.dateTime desc")
    List<BrokerAssetOhlcvEntity> findAllByBrokerIdAndAssetIdAndType(
            @Param("brokerId")String brokerId,
            @Param("assetId")String assetId,
            @Param("type")Ohlcv.Type type,
            @Param("dateTimeFrom")LocalDateTime dateTimeFrom,
            @Param("dateTimeTo")LocalDateTime dateTimeTo,
            Pageable pageable
    );

}
