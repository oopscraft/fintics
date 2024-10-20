package org.oopscraft.fintics.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.OhlcvSummary;

import java.util.List;

@Mapper
public interface DataMapper {

    List<Asset> selectAssets(@Param("assetId") String assetId, @Param("name") String name, @Param("market") String market, RowBounds rowBounds);

    List<OhlcvSummary> selectOhlcvSummaries(@Param("assetId") String assetId);

    List<OhlcvSummary.OhlcvStatistic> selectOhlcvStatistics(@Param("assetId") String assetId);

}
