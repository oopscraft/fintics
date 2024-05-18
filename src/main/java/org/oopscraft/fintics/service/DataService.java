package org.oopscraft.fintics.service;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                .orderBy(Stream.of(
                        Optional.ofNullable(pageable.getSort().getOrderFor(AssetEntity_.PER))
                                .map(order -> order.getDirection() == Sort.Direction.DESC ? qAssetEntity.per.desc() : qAssetEntity.per.asc())
                                .orElse(null),
                        Optional.ofNullable(pageable.getSort().getOrderFor(AssetEntity_.ROE))
                                .map(order -> order.getDirection() == Sort.Direction.DESC ? qAssetEntity.roe.desc() : qAssetEntity.roe.asc())
                                .orElse(null),
                        qAssetEntity.marketCap.desc()
                        ).filter(Objects::nonNull)
                        .toArray(OrderSpecifier[]::new))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        return assetEntities.stream()
                .map(Asset::from)
                .collect(Collectors.toList());
    }

    public List<OhlcvSummary> getAssetOhlcvSummaries() {
        return dataMapper.selectAssetOhlcvSummaries(null).stream()
                .peek(it -> it.setName(assetService.getAsset(it.getId())
                        .map(Asset::getAssetName)
                        .orElse(null)))
                .toList();
    }

    public Optional<OhlcvSummary> getAssetOhlcvSummary(String assetId) {
        OhlcvSummary assetOhlcvSummary = dataMapper.selectAssetOhlcvSummaries(assetId).stream()
                .findFirst()
                .orElseThrow();
        assetOhlcvSummary.setName(assetService.getAsset(assetOhlcvSummary.getId())
                .map(Asset::getAssetName)
                .orElse(null));
        List<OhlcvSummary.OhlcvStatistic> ohlcvStatistics = dataMapper.selectAssetOhlcvStatistics(assetId);
        assetOhlcvSummary.setOhlcvStatistics(ohlcvStatistics);
        return Optional.of(assetOhlcvSummary);
    }

    public List<Ohlcv> getAssetOhlcvs(String assetId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
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
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    public List<OhlcvSummary> getIndiceOhlcvSummaries() {
        return dataMapper.selectIndiceOhlcvSummaries(null).stream()
                .peek(it -> it.setName(indiceService.getIndice(it.getId())
                        .map(Indice::getIndiceName)
                        .orElse(null)))
                .toList();
    }

    public Optional<OhlcvSummary> getIndiceOhlcvSummary(Indice.Id indiceId) {
        OhlcvSummary indiceOhlcvSummary = dataMapper.selectIndiceOhlcvSummaries(indiceId).stream()
                .findFirst()
                .orElseThrow();
        indiceOhlcvSummary.setName(indiceService.getIndice(indiceOhlcvSummary.getId())
                .map(Indice::getIndiceName)
                .orElse(null));
        List<OhlcvSummary.OhlcvStatistic> ohlcvStatistics = dataMapper.selectIndiceOhlcvStatistics(indiceId);
        indiceOhlcvSummary.setOhlcvStatistics(ohlcvStatistics);
        return Optional.of(indiceOhlcvSummary);
    }

    public List<Ohlcv> getIndiceOhlcvs(Indice.Id indiceId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
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
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

}
