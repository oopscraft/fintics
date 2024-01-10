package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.IndiceId;
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
public interface IndiceOhlcvRepository extends JpaRepository<IndiceOhlcvEntity, IndiceOhlcvEntity.Pk> {

    @Query("select max(a.dateTime) from IndiceOhlcvEntity a" +
            " where a.indiceId = :indiceId" +
            " and a.ohlcvType = :ohlcvType")
    Optional<LocalDateTime> findMaxDateTimeByIndiceIdAndOhlcvType(
            @Param("indiceId") IndiceId indiceId,
            @Param("ohlcvType")OhlcvType ohlcvType
    );

    @Query("select a from IndiceOhlcvEntity a" +
            " where a.indiceId = :indiceId" +
            " and a.ohlcvType = :ohlcvType" +
            " and a.dateTime between :dateTimeFrom and :dateTimeTo" +
            " order by a.dateTime desc")
    List<IndiceOhlcvEntity> findAllByIndiceIdAndOhlcvType(
            @Param("indiceId") IndiceId indiceId,
            @Param("ohlcvType")OhlcvType ohlcvType,
            @Param("dateTimeFrom")LocalDateTime dateTimeFrom,
            @Param("dateTimeTo")LocalDateTime dateTimeTo,
            Pageable pageable
    );

}
