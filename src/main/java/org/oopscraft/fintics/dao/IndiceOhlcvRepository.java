package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.IndiceSymbol;
import org.oopscraft.fintics.model.OhlcvType;
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
            " where a.symbol = :symbol" +
            " and a.ohlcvType = :ohlcvType")
    Optional<LocalDateTime> findMaxDateTimeBySymbolAndOhlcvType(@Param("symbol") IndiceSymbol symbol, @Param("ohlcvType")OhlcvType ohlcvType);

    @Query("select a from IndiceOhlcvEntity a" +
            " where a.symbol = :symbol" +
            " and a.ohlcvType = :ohlcvType" +
            " and a.dateTime between :dateTimeFrom and :dateTimeTo" +
            " order by a.dateTime desc")
    List<IndiceOhlcvEntity> findAllBySymbolAndOhlcvType(@Param("symbol") IndiceSymbol symbol, @Param("ohlcvType")OhlcvType ohlcvType, @Param("dateTimeFrom")LocalDateTime dateTimeFrom, @Param("dateTimeTo")LocalDateTime dateTimeTo);

}
