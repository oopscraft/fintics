package org.oopscraft.fintics.service;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.spi.CopyOnWrite;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.scheduler.PastOhlcvCollector;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataService {

    private final JPAQueryFactory jpaQueryFactory;

    private final DataMapper dataMapper;

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

    public synchronized List<AssetOhlcvSummary> getAssetOhlcvSummaries() {
        return dataMapper.selectAssetOhlcvSummaries();
    }

    public List<AssetOhlcv> getAssetOhlcvs(String assetId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Boolean interpolated, Pageable pageable) {
        QAssetOhlcvEntity qAssetOhlcvEntity = QAssetOhlcvEntity.assetOhlcvEntity;
        JPAQuery<AssetOhlcvEntity> query = jpaQueryFactory
                .selectFrom(qAssetOhlcvEntity)
                .where(
                        Optional.ofNullable(assetId).map(qAssetOhlcvEntity.assetId::eq).orElse(null),
                        Optional.ofNullable(type).map(qAssetOhlcvEntity.type::eq).orElse(null),
                        Optional.ofNullable(dateTimeFrom).map(qAssetOhlcvEntity.dateTime::goe).orElse(null),
                        Optional.ofNullable(dateTimeTo).map(qAssetOhlcvEntity.dateTime::loe).orElse(null),
                        Optional.ofNullable(interpolated).map(qAssetOhlcvEntity.interpolated::eq).orElse(null)
                )
                .orderBy(qAssetOhlcvEntity.dateTime.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset());
        List<AssetOhlcvEntity> assetOhlcvEntities = query.fetch();
        return assetOhlcvEntities.stream()
                .map(AssetOhlcv::from)
                .collect(Collectors.toList());
    }

    public List<IndiceOhlcvSummary> getIndiceOhlcvSummaries() {
        return dataMapper.selectIndiceOhlcvSummaries();
    }

    public List<IndiceOhlcv> getIndiceOhlcvs(Indice.Id indiceId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Boolean interpolated, Pageable pageable) {
        QIndiceOhlcvEntity qIndiceOhlcvEntity = QIndiceOhlcvEntity.indiceOhlcvEntity;
        JPAQuery<IndiceOhlcvEntity> query = jpaQueryFactory
                .selectFrom(qIndiceOhlcvEntity)
                .where(
                        Optional.ofNullable(indiceId).map(qIndiceOhlcvEntity.indiceId::eq).orElse(null),
                        Optional.ofNullable(type).map(qIndiceOhlcvEntity.type::eq).orElse(null),
                        Optional.ofNullable(dateTimeFrom).map(qIndiceOhlcvEntity.dateTime::goe).orElse(null),
                        Optional.ofNullable(dateTimeTo).map(qIndiceOhlcvEntity.dateTime::loe).orElse(null),
                        Optional.ofNullable(interpolated).map(qIndiceOhlcvEntity.interpolated::eq).orElse(null)
                )
                .orderBy(qIndiceOhlcvEntity.dateTime.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset());
        List<IndiceOhlcvEntity> indiceOhlcvEntities = query.fetch();
        return indiceOhlcvEntities.stream()
                .map(IndiceOhlcv::from)
                .collect(Collectors.toList());
    }

}
