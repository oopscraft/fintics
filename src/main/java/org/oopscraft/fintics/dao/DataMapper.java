package org.oopscraft.fintics.dao;

import org.apache.ibatis.annotations.Mapper;
import org.oopscraft.fintics.model.AssetOhlcvSummary;
import org.oopscraft.fintics.model.IndiceOhlcvSummary;

import java.util.List;

@Mapper
public interface DataMapper {

    List<AssetOhlcvSummary> selectAssetOhlcvSummaries();

    List<IndiceOhlcvSummary> selectIndiceOhlcvSummaries();

}
