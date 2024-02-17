package org.oopscraft.fintics.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.oopscraft.fintics.model.DataSummary;
import org.oopscraft.fintics.model.Ohlcv;

import java.util.List;

@Mapper
public interface DataMapper {

    List<DataSummary.AssetOhlcvStatistics> selectAssetOhlcvStatistics(@Param("type") Ohlcv.Type type);

    List<DataSummary.IndiceOhlcvStatistics> selectIndiceOhlcvStatistics(@Param("type") Ohlcv.Type type);

}
