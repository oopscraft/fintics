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

    public List<NewsSummary> getAssetNewsSummaries() {
        return dataMapper.selectAssetNewsSummaries(null).stream()
                .peek(it -> it.setName(assetService.getAsset(it.getId())
                        .map(Asset::getAssetName)
                        .orElse(null)))
                .toList();
    }

    public Optional<NewsSummary> getAssetNewsSummary(String assetId) {
        NewsSummary newsSummary = dataMapper.selectAssetNewsSummaries(assetId).stream()
                .findFirst()
                .orElseThrow();
        newsSummary.setName(assetService.getAsset(newsSummary.getId())
                .map(Asset::getAssetName)
                .orElse(null));
        List<NewsSummary.NewsStatistic> newsStatistics = dataMapper.selectAssetNewsStatistics(assetId);
        newsSummary.setNewsStatisticList(newsStatistics);
        return Optional.of(newsSummary);
    }

    public List<NewsSummary> getIndiceNewsSummaries() {
        return dataMapper.selectIndiceNewsSummaries(null).stream()
                .peek(it-> it.setName(indiceService.getIndice(it.getId())
                        .map(Indice::getIndiceName)
                        .orElse(null)))
                .toList();
    }

    public Optional<NewsSummary> getIndiceNewsSummary(Indice.Id indiceId) {
        NewsSummary newsSummary = dataMapper.selectIndiceNewsSummaries(indiceId).stream()
                .findFirst()
                .orElseThrow();
        newsSummary.setName(indiceService.getIndice(newsSummary.getId())
                .map(Indice::getIndiceName)
                .orElse(null));
        List<NewsSummary.NewsStatistic> newsStatistics = dataMapper.selectIndiceNewsStatistics(indiceId);
        newsSummary.setNewsStatisticList(newsStatistics);
        return Optional.of(newsSummary);
    }

}
