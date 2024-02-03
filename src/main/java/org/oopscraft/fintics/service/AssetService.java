package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    private final AssetOhlcvRepository assetOhlcvRepository;

    public Page<Asset> getAssets(AssetSearch assetSearch, Pageable pageable) {
        Page<AssetEntity> brokerAssetEntityPage = assetRepository.findAllBy(assetSearch, pageable);
        List<Asset> brokerAssets = brokerAssetEntityPage.getContent().stream()
                .map(Asset::from)
                .toList();
        long total = brokerAssetEntityPage.getTotalElements();
        return new PageImpl<>(brokerAssets, pageable, total);
    }

    public Optional<Asset> getAsset(String assetId) {
        Asset brokerAsset = assetRepository.findById(assetId)
                .map(Asset::from)
                .orElseThrow();
        return Optional.of(brokerAsset);
    }

    public Optional<AssetIndicator> getAssetIndicator(String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        AssetEntity brokerAssetEntity = assetRepository.findById(assetId)
                .orElse(null);
        String assetName = (brokerAssetEntity != null ? brokerAssetEntity.getAssetName() : assetId);

        // minute ohlcv
        LocalDateTime minuteDateTimeTo = Optional.ofNullable(dateTimeTo)
                .orElse(assetOhlcvRepository.findMaxDateTimeByAssetIdAndType(assetId, Ohlcv.Type.MINUTE)
                        .orElse(LocalDateTime.now()));
        LocalDateTime minuteDateTimeFrom = Optional.ofNullable(dateTimeFrom)
                .orElse(minuteDateTimeTo.minusDays(1));
        List<Ohlcv> minuteOhlcvs = assetOhlcvRepository.findAllByAssetIdAndType(assetId, Ohlcv.Type.MINUTE, minuteDateTimeFrom, minuteDateTimeTo, Pageable.unpaged())
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        // daily ohlcv
        LocalDateTime dailyDateTimeTo = Optional.ofNullable(dateTimeTo)
                .orElse(assetOhlcvRepository.findMaxDateTimeByAssetIdAndType(assetId, Ohlcv.Type.DAILY)
                        .orElse(LocalDateTime.now()));
        LocalDateTime dailyDateTimeFrom = Optional.ofNullable(dateTimeFrom)
                .orElse(dailyDateTimeTo.minusMonths(1));
        List<Ohlcv> dailyOhlcvs = assetOhlcvRepository.findAllByAssetIdAndType(assetId, Ohlcv.Type.DAILY, dailyDateTimeFrom, dailyDateTimeTo, Pageable.unpaged())
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        return Optional.ofNullable(AssetIndicator.builder()
                .assetId(assetId)
                .assetName(assetName)
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .build());
    }

}
