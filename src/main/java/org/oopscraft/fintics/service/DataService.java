package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataService {

    private final DataMapper dataMapper;

    private final AssetService assetService;

    /**
     * returns assets
     * @param assetId asset id
     * @param name asset name
     * @param market market
     * @param pageable pageable
     * @return list of assets
     */
    public List<Asset> getAssets(String assetId, String name, String market, Pageable pageable) {
        return dataMapper.selectAssets(
                Optional.ofNullable(assetId).map(value -> '%' + value +'%').orElse(null),
                Optional.ofNullable(name).map(value -> '%' + value + '%').orElse(null),
                market,
                new RowBounds((int)pageable.getOffset(), pageable.getPageSize())
        );
    }

    /**
     * returns ohlcv summaries
     * @return ohlcv summaries
     */
    public List<OhlcvSummary> getOhlcvSummaries() {
        return dataMapper.selectOhlcvSummaries(null).stream()
                .peek(it -> it.setName(assetService.getAsset(it.getAssetId())
                        .map(Asset::getName)
                        .orElse(null)))
                .toList();
    }

    /**
     * returns ohlcv summary
     * @param assetId asset id
     * @return ohlcv summary
     */
    public Optional<OhlcvSummary> getOhlcvSummary(String assetId) {
        OhlcvSummary assetOhlcvSummary = dataMapper.selectOhlcvSummaries(assetId).stream()
                .findFirst()
                .orElseThrow();
        assetOhlcvSummary.setName(assetService.getAsset(assetOhlcvSummary.getAssetId())
                .map(Asset::getName)
                .orElse(null));
        List<OhlcvSummary.OhlcvStatistic> ohlcvStatistics = dataMapper.selectOhlcvStatistics(assetId);
        assetOhlcvSummary.setOhlcvStatistics(ohlcvStatistics);
        return Optional.of(assetOhlcvSummary);
    }

}
