package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    private final AssetOhlcvRepository assetOhlcvRepository;

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
        return assetOhlcvRepository.findAllByAssetIdAndType(assetId, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(Ohlcv::from)
                .toList();
    }

    public List<Ohlcv> getAssetMinuteOhlcvs(String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        return assetOhlcvRepository.findAllByAssetIdAndType(assetId, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(Ohlcv::from)
                .toList();
    }

    public List<News> getAssetNewses(String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        return assetNewsRepository.findAllByAssetId(assetId, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(News::from)
                .toList();
    }

}
