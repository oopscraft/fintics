package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.IndiceId;
import org.oopscraft.fintics.model.Ohlcv;
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
public interface IndiceOhlcvRepository extends JpaRepository<IndiceOhlcvEntity, IndiceOhlcvEntity.Pk>, JpaSpecificationExecutor<IndiceOhlcvEntity> {

    @Query("select max(a.dateTime) from IndiceOhlcvEntity a" +
            " where a.indiceId = :indiceId" +
            " and a.type = :type")
    Optional<LocalDateTime> findMaxDateTimeByIndiceIdAndType(
            @Param("indiceId") IndiceId indiceId,
            @Param("type") Ohlcv.Type type
    );

    @Query("select a from IndiceOhlcvEntity a" +
            " where a.indiceId = :indiceId" +
            " and a.type = :type" +
            " and a.dateTime between :dateTimeFrom and :dateTimeTo" +
            " order by a.dateTime desc")
    List<IndiceOhlcvEntity> findAllByIndiceIdAndType(
            @Param("indiceId") IndiceId indiceId,
            @Param("type") Ohlcv.Type type,
            @Param("dateTimeFrom") LocalDateTime dateTimeFrom,
            @Param("dateTimeTo") LocalDateTime dateTimeTo,
            Pageable pageable
    );

}
