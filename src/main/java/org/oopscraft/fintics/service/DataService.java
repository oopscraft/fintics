package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.api.v1.dto.IndiceOhlcvResponse;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataService {

    private final AssetRepository assetRepository;

    private final AssetOhlcvRepository assetOhlcvRepository;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    public Page<Asset> getAssets(String assetId, String assetName, Asset.Type type, Pageable pageable) {
        // where
        Specification<AssetEntity> specification = Specification.where(null);
        specification = specification
                .and(Optional.ofNullable(assetId).map(AssetSpecifications::containsAssetId).orElse(null))
                .and(Optional.ofNullable(assetName).map(AssetSpecifications::containsAssetName).orElse(null))
                .and(Optional.ofNullable(type).map(AssetSpecifications::equalType).orElse(null));

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

    public Page<AssetOhlcv> getAssetOhlcvs(String assetId, Ohlcv.Type type, Pageable pageable) {
        // where
        Specification<AssetOhlcvEntity> specification = Specification.where(null);
        specification = specification
                .and(Optional.ofNullable(assetId).map(AssetOhlcvSpecifications::equalAssetId).orElse(null))
                .and(Optional.ofNullable(type).map(AssetOhlcvSpecifications::equalType).orElse(null));

        // sort
        Sort sort = Sort.by(AssetOhlcvEntity_.TYPE).descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // find
        Page<AssetOhlcvEntity> assetOhlcvEntityPage = assetOhlcvRepository.findAll(specification, pageable);
        List<AssetOhlcv> assetOhlcvs = assetOhlcvEntityPage.getContent().stream()
                .map(AssetOhlcv::from)
                .collect(Collectors.toList());
        long total =  assetOhlcvEntityPage.getTotalElements();
        return new PageImpl<>(assetOhlcvs, pageable, total);
    }

    public Page<IndiceOhlcv> getIndiceOhlcvs(IndiceId indiceId, Ohlcv.Type type, Pageable pageable) {
        // where
        Specification<IndiceOhlcvEntity> specification = Specification.where(null);
        specification = specification
                .and(Optional.ofNullable(indiceId).map(IndiceOhlcvSpecification::equalIndiceId).orElse(null))
                .and(Optional.ofNullable(type).map(IndiceOhlcvSpecification::equalType).orElse(null));

        // sort
        Sort sort = Sort.by(IndiceOhlcvEntity_.DATE_TIME).descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // find
        Page<IndiceOhlcvEntity> indiceOhlcvEntityPage = indiceOhlcvRepository.findAll(specification, pageable);
        List<IndiceOhlcv> indiceOhlcvs = indiceOhlcvEntityPage.getContent().stream()
                .map(IndiceOhlcv::from)
                .collect(Collectors.toList());
        long total = indiceOhlcvEntityPage.getTotalElements();
        return new PageImpl<>(indiceOhlcvs, pageable, total);
    }



}
