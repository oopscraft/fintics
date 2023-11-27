package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.OhlcvType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeAssetOhlcvRepository extends JpaRepository<TradeAssetOhlcvEntity, TradeAssetOhlcvEntity.Pk> {

    @Query("select max(a.dateTime) from TradeAssetOhlcvEntity a " +
            " where a.tradeId = :tradeId" +
            " and a.symbol = :symbol" +
            " and a.ohlcvType = :ohlcvType")
    Optional<LocalDateTime> findMaxDateTimeBySymbolAndOhlcvType(@Param("tradeId")String tradeId, @Param("symbol")String symbol, @Param("ohlcvType")OhlcvType ohlcvType);

    @Query("select a from TradeAssetOhlcvEntity a " +
            " where a.tradeId = :tradeId" +
            " and a.symbol = :symbol" +
            " and a.ohlcvType = :ohlcvType" +
            " and a.dateTime between :dateTimeFrom and :dateTimeTo" +
            " order by a.dateTime desc")
    List<TradeAssetOhlcvEntity> findAllBySymbolAndOhlcvType(@Param("tradeId")String tradeId, @Param("symbol")String symbol, @Param("ohlcvType")OhlcvType ohlcvType, @Param("dateTimeFrom")LocalDateTime dateTimeFrom, @Param("dateTimeTo")LocalDateTime dateTimeTo);

}
