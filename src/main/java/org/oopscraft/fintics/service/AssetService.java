package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    private final AssetOhlcvRepository assetOhlcvRepository;

    private final AssetOhlcvSplitRepository assetOhlcvSplitRepository;

    private final AssetNewsRepository assetNewsRepository;

    public Page<Asset> getAssets(String assetId, String assetName, String market, Boolean favorite, BigDecimal perFrom, BigDecimal perTo, BigDecimal roeFrom, BigDecimal roeTo, BigDecimal roaFrom, BigDecimal roaTo, Pageable pageable) {
        // where
        Specification<AssetEntity> specification = Specification.where(null);
        specification = specification
                .and(Optional.ofNullable(assetId)
                        .map(AssetSpecifications::containsAssetId)
                        .orElse(null))
                .and(Optional.ofNullable(assetName)
                        .map(AssetSpecifications::containsAssetName)
                        .orElse(null))
                .and(Optional.ofNullable(market)
                        .map(AssetSpecifications::equalMarket)
                        .orElse(null))
                .and(Optional.ofNullable(favorite)
                        .map(AssetSpecifications::isFavorite)
                        .orElse(null));
        if (perFrom != null && perTo != null) {
            specification = specification.and(AssetSpecifications.betweenPer(perFrom, perTo));
        }
        if (roeFrom != null && roeTo != null) {
            specification = specification.and(AssetSpecifications.betweenRoe(roeFrom, roeTo));
        }
        if (roaFrom != null && roaTo != null) {
            specification = specification.and(AssetSpecifications.betweenRoa(roaFrom, roaTo));
        }

        // sort
        Sort sort = Sort.by(AssetEntity_.MARKET_CAP).descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // find
        Page<AssetEntity> assetEntityPage = assetRepository.findAll(specification, pageable);
        List<Asset> assets = assetEntityPage.getContent().stream()
                .map(Asset::from)
                .toList();
        long total = assetEntityPage.getTotalElements();
        return new PageImpl<>(assets, pageable, total);
    }

    public Optional<Asset> getAsset(String assetId) {
        Asset brokerAsset = assetRepository.findById(assetId)
                .map(Asset::from)
                .orElseThrow();
        return Optional.of(brokerAsset);
    }

    public List<Ohlcv> getAssetDailyOhlcvs(String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        List<Ohlcv> assetDailyOhlcvs = assetOhlcvRepository.findAllByAssetIdAndType(assetId, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(Ohlcv::from)
                .toList();

        // apply split ratio
        applySplitRatioIfExist(assetId, assetDailyOhlcvs);

        // return
        return assetDailyOhlcvs;
    }

    public List<Ohlcv> getAssetMinuteOhlcvs(String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        List<Ohlcv> assetMinuteOhlcvs = assetOhlcvRepository.findAllByAssetIdAndType(assetId, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(Ohlcv::from)
                .toList();

        // apply split ratio
        applySplitRatioIfExist(assetId, assetMinuteOhlcvs);

        // return
        return assetMinuteOhlcvs;
    }

    void applySplitRatioIfExist(String assetId, List<Ohlcv> ohlcvs) {
        // ohlcv split data
        LocalDateTime dateTimeFrom = ohlcvs.stream()
                .map(Ohlcv::getDateTime)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        LocalDateTime dateTimeTo = ohlcvs.stream()
                .map(Ohlcv::getDateTime)
                .max(Comparator.naturalOrder())
                .orElseThrow();
        List<AssetOhlcvSplitEntity> assetOhlcvSplitEntities = assetOhlcvSplitRepository.findAllByAssetId(assetId, dateTimeFrom, dateTimeTo);

        // if split data exists
        if (!assetOhlcvSplitEntities.isEmpty()) {
            // prepare split ratio map
            NavigableMap<LocalDateTime, BigDecimal> cumulativeRatios = calculateCumulativeRatios(assetOhlcvSplitEntities);

            // adjust split to ohlcv
            for (Ohlcv ohlcv : ohlcvs) {
                BigDecimal splitRatio = getCumulativeRatioForDate(ohlcv.getDateTime(), cumulativeRatios);
                ohlcv.setOpenPrice(ohlcv.getOpenPrice().divide(splitRatio, MathContext.DECIMAL32));
                ohlcv.setHighPrice(ohlcv.getHighPrice().divide(splitRatio, MathContext.DECIMAL32));
                ohlcv.setLowPrice(ohlcv.getLowPrice().divide(splitRatio, MathContext.DECIMAL32));
                ohlcv.setClosePrice(ohlcv.getClosePrice().divide(splitRatio, MathContext.DECIMAL32));
                ohlcv.setVolume(ohlcv.getVolume().multiply(splitRatio));
            }
        }
    }

    NavigableMap<LocalDateTime, BigDecimal> calculateCumulativeRatios(List<AssetOhlcvSplitEntity> splitEntities) {
        NavigableMap<LocalDateTime, BigDecimal> cumulativeRatios = new TreeMap<>();
        BigDecimal cumulativeRatio = BigDecimal.ONE;
        for (AssetOhlcvSplitEntity split : splitEntities) {
            BigDecimal splitRatio = BigDecimal.ONE;
            // forward split
            if (split.getSplitTo().compareTo(split.getSplitFrom()) > 0) {
                splitRatio = split.getSplitTo().divide(split.getSplitFrom(), MathContext.DECIMAL32);
            }
            // reverse split
            if (split.getSplitTo().compareTo(split.getSplitFrom()) < 0) {
                splitRatio = split.getSplitTo().multiply(split.getSplitFrom());
            }
            cumulativeRatio = cumulativeRatio.multiply(splitRatio);
            cumulativeRatios.put(split.getDateTime(), cumulativeRatio);
        }
        return cumulativeRatios;
    }

    BigDecimal getCumulativeRatioForDate(LocalDateTime dateTime, NavigableMap<LocalDateTime, BigDecimal> cumulativeRatios) {
        return cumulativeRatios.tailMap(dateTime, false).values().stream()
                .reduce(BigDecimal.ONE, BigDecimal::multiply);
    }

    public List<News> getAssetNewses(String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        return assetNewsRepository.findAllByAssetId(assetId, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(News::from)
                .toList();
    }

}
