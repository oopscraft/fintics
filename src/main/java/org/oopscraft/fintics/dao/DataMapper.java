package org.oopscraft.fintics.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.OhlcvSummary;

import java.util.List;

@Mapper
public interface DataMapper {

    List<OhlcvSummary> selectAssetOhlcvSummaries(@Param("assetId") String assetId);

    List<OhlcvSummary> selectIndiceOhlcvSummaries(@Param("indiceId")Indice.Id indiceId);

    List<OhlcvSummary.OhlcvStatistic> selectAssetOhlcvStatistics(@Param("assetId") String assetId);

    List<OhlcvSummary.OhlcvStatistic> selectIndiceOhlcvStatistics(@Param("indiceId") Indice.Id indiceId);

}
