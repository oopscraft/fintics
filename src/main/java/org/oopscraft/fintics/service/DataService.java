package org.oopscraft.fintics.service;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataService {

    private final JPAQueryFactory jpaQueryFactory;

    private final DataMapper dataMapper;

    private final OhlcvClient ohlcvClient;

    private final AssetService assetService;

    private final AssetOhlcvRepository assetOhlcvRepository;

    private final IndiceService indiceService;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    public List<Asset> getAssets(String assetId, String assetName, String market, Pageable pageable) {
        QAssetEntity qAssetEntity = QAssetEntity.assetEntity;
        List<AssetEntity> assetEntities = jpaQueryFactory
                .selectFrom(qAssetEntity)
                .where(
                        Optional.ofNullable(assetId).map(qAssetEntity.assetId::contains).orElse(null),
                        Optional.ofNullable(assetName).map(qAssetEntity.assetName::contains).orElse(null),
                        Optional.ofNullable(market).map(qAssetEntity.market::eq).orElse(null)
                )
                .orderBy(qAssetEntity.marketCap.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();
        return assetEntities.stream()
                .map(Asset::from)
                .collect(Collectors.toList());
    }

    public List<AssetOhlcvSummary> getAssetOhlcvSummaries() {
        return dataMapper.selectAssetOhlcvSummaries(null);
    }

    public Optional<AssetOhlcvSummary> getAssetOhlcvSummary(String assetId) {
        AssetOhlcvSummary assetOhlcvSummary = dataMapper.selectAssetOhlcvSummaries(assetId).stream()
                .findFirst()
                .orElseThrow();
        List<OhlcvSummary.OhlcvStatistic> ohlcvStatistics = dataMapper.selectAssetOhlcvStatistics(assetId);
        assetOhlcvSummary.setOhlcvStatistics(ohlcvStatistics);
        return Optional.of(assetOhlcvSummary);
    }

    public List<AssetOhlcv> getAssetOhlcvs(String assetId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        QAssetOhlcvEntity qAssetOhlcvEntity = QAssetOhlcvEntity.assetOhlcvEntity;
        JPAQuery<AssetOhlcvEntity> query = jpaQueryFactory
                .selectFrom(qAssetOhlcvEntity)
                .where(
                        Optional.ofNullable(assetId).map(qAssetOhlcvEntity.assetId::eq).orElse(null),
                        Optional.ofNullable(type).map(qAssetOhlcvEntity.type::eq).orElse(null),
                        Optional.ofNullable(dateTimeFrom).map(qAssetOhlcvEntity.dateTime::goe).orElse(null),
                        Optional.ofNullable(dateTimeTo).map(qAssetOhlcvEntity.dateTime::loe).orElse(null)
                )
                .orderBy(qAssetOhlcvEntity.dateTime.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset());
        List<AssetOhlcvEntity> assetOhlcvEntities = query.fetch();
        return assetOhlcvEntities.stream()
                .map(AssetOhlcv::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void interpolateAssetOhlcvs(String assetId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        Asset asset = assetService.getAsset(assetId).orElseThrow();
        List<Ohlcv> ohlcvs = ohlcvClient.getAssetOhlcvs(asset, type, dateTimeFrom, dateTimeTo);
        for (Ohlcv ohlcv : ohlcvs) {
            AssetOhlcvEntity.Pk pk = AssetOhlcvEntity.Pk.builder()
                    .assetId(asset.getAssetId())
                    .type(ohlcv.getType())
                    .dateTime(ohlcv.getDateTime())
                    .build();
            AssetOhlcvEntity assetOhlcvEntity = assetOhlcvRepository.findById(pk).orElse(null);
            if (assetOhlcvEntity == null) {
                assetOhlcvEntity = AssetOhlcvEntity.builder()
                        .assetId(asset.getAssetId())
                        .type(ohlcv.getType())
                        .dateTime(ohlcv.getDateTime())
                        .openPrice(ohlcv.getOpenPrice())
                        .highPrice(ohlcv.getHighPrice())
                        .lowPrice(ohlcv.getLowPrice())
                        .closePrice(ohlcv.getClosePrice())
                        .volume(ohlcv.getVolume())
                        .build();
                assetOhlcvRepository.saveAndFlush(assetOhlcvEntity);
            }
        }
    }

    public List<IndiceOhlcvSummary> getIndiceOhlcvSummaries() {
        return dataMapper.selectIndiceOhlcvSummaries(null);
    }

    public Optional<IndiceOhlcvSummary> getIndiceOhlcvSummary(Indice.Id indiceId) {
        IndiceOhlcvSummary indiceOhlcvSummary = dataMapper.selectIndiceOhlcvSummaries(indiceId).stream()
                .findFirst()
                .orElseThrow();
        List<OhlcvSummary.OhlcvStatistic> ohlcvStatistics = dataMapper.selectIndiceOhlcvStatistics(indiceId);
        indiceOhlcvSummary.setOhlcvStatistics(ohlcvStatistics);
        return Optional.of(indiceOhlcvSummary);
    }

    public List<IndiceOhlcv> getIndiceOhlcvs(Indice.Id indiceId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        QIndiceOhlcvEntity qIndiceOhlcvEntity = QIndiceOhlcvEntity.indiceOhlcvEntity;
        JPAQuery<IndiceOhlcvEntity> query = jpaQueryFactory
                .selectFrom(qIndiceOhlcvEntity)
                .where(
                        Optional.ofNullable(indiceId).map(qIndiceOhlcvEntity.indiceId::eq).orElse(null),
                        Optional.ofNullable(type).map(qIndiceOhlcvEntity.type::eq).orElse(null),
                        Optional.ofNullable(dateTimeFrom).map(qIndiceOhlcvEntity.dateTime::goe).orElse(null),
                        Optional.ofNullable(dateTimeTo).map(qIndiceOhlcvEntity.dateTime::loe).orElse(null)
                )
                .orderBy(qIndiceOhlcvEntity.dateTime.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset());
        List<IndiceOhlcvEntity> indiceOhlcvEntities = query.fetch();
        return indiceOhlcvEntities.stream()
                .map(IndiceOhlcv::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void interpolateIndiceOhlcvs(Indice.Id indiceId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        Indice indice = indiceService.getIndice(indiceId.name()).orElseThrow();
        List<Ohlcv> ohlcvs = ohlcvClient.getIndiceOhlcvs(indice, type, dateTimeFrom, dateTimeTo);
        for (Ohlcv ohlcv : ohlcvs) {
            IndiceOhlcvEntity.Pk pk = IndiceOhlcvEntity.Pk.builder()
                    .indiceId(indiceId)
                    .type(ohlcv.getType())
                    .dateTime(ohlcv.getDateTime())
                    .build();
            IndiceOhlcvEntity indiceOhlcvEntity = indiceOhlcvRepository.findById(pk).orElse(null);
            if (indiceOhlcvEntity == null) {
                indiceOhlcvEntity = IndiceOhlcvEntity.builder()
                        .indiceId(indiceId)
                        .type(ohlcv.getType())
                        .dateTime(ohlcv.getDateTime())
                        .openPrice(ohlcv.getOpenPrice())
                        .highPrice(ohlcv.getHighPrice())
                        .lowPrice(ohlcv.getLowPrice())
                        .closePrice(ohlcv.getClosePrice())
                        .volume(ohlcv.getVolume())
                        .build();
                indiceOhlcvRepository.saveAndFlush(indiceOhlcvEntity);
            }
        }
    }

}
