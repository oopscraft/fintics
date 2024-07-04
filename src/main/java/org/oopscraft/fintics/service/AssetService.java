package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    private final NewsRepository newsRepository;

    public Page<Asset> getAssets(AssetSearch assetSearch, Pageable pageable) {
        Page<AssetEntity> assetEntityPage = assetRepository.findAll(assetSearch, pageable);
        List<Asset> assets = assetEntityPage.getContent().stream()
                .map(Asset::from)
                .toList();
        long total = assetEntityPage.getTotalElements();
        return new PageImpl<>(assets, pageable, total);
    }

    public Optional<Asset> getAsset(String assetId) {
        return assetRepository.findById(assetId)
                .map(Asset::from);
    }

    public List<News> getNewses(String assetId, LocalDateTime datetimeFrom, LocalDateTime datetimeTo, Pageable pageable) {
        return newsRepository.findAllByAssetId(assetId, datetimeFrom, datetimeTo, pageable).stream()
                .map(News::from)
                .toList();
    }

}
