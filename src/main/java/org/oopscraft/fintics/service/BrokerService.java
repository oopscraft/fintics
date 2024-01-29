package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrokerService {

    private final BrokerClientFactory brokerClientFactory;

    private final BrokerAssetRepository brokerAssetRepository;

    private final BrokerAssetOhlcvRepository brokerAssetOhlcvRepository;

    public List<Broker> getBrokers() {
        return brokerClientFactory.getBrokerClientDefinitions().stream()
                .map(Broker::from)
                .toList();
    }

    public Optional<Broker> getBroker(String brokerId) {
        return brokerClientFactory.getBrokerClientDefinition(brokerId)
                .map(Broker::from);
    }

    public Page<BrokerAsset> getBrokerAssets(String brokerId, String assetId, String assetName, Pageable pageable) {
        Page<BrokerAssetEntity> brokerAssetEntityPage = brokerAssetRepository.findAllBy(brokerId, assetId, assetName, pageable);
        List<BrokerAsset> brokerAssets = brokerAssetEntityPage.getContent().stream()
                .map(BrokerAsset::from)
                .toList();
        long total = brokerAssetEntityPage.getTotalElements();
        return new PageImpl<>(brokerAssets, pageable, total);
    }

    public Optional<BrokerAsset> getBrokerAssets(String brokerId, String assetId) {
        return Optional.empty();
    }

    public Optional<AssetIndicator> getAssetIndicator(String brokerId, String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        BrokerAssetEntity.Pk brokerAssetPk = BrokerAssetEntity.Pk.builder()
                .brokerId(brokerId)
                .assetId(assetId)
                .build();
        BrokerAssetEntity brokerAssetEntity = brokerAssetRepository.findById(brokerAssetPk).orElseThrow();

        // minute ohlcv
        LocalDateTime minuteDateTimeTo = Optional.ofNullable(dateTimeTo)
                .orElse(brokerAssetOhlcvRepository.findMaxDateTimeByBrokerIdAndAssetIdAndType(brokerId, assetId, Ohlcv.Type.MINUTE)
                        .orElse(LocalDateTime.now()));
        LocalDateTime minuteDateTimeFrom = Optional.ofNullable(dateTimeFrom)
                .orElse(minuteDateTimeTo.minusDays(1));
        List<Ohlcv> minuteOhlcvs = brokerAssetOhlcvRepository.findAllByBrokerIdAndAssetIdAndType(brokerId, assetId, Ohlcv.Type.MINUTE, minuteDateTimeFrom, minuteDateTimeTo, Pageable.unpaged())
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        // daily ohlcv
        LocalDateTime dailyDateTimeTo = Optional.ofNullable(dateTimeTo)
                .orElse(brokerAssetOhlcvRepository.findMaxDateTimeByBrokerIdAndAssetIdAndType(brokerId, assetId, Ohlcv.Type.DAILY)
                        .orElse(LocalDateTime.now()));
        LocalDateTime dailyDateTimeFrom = Optional.ofNullable(dateTimeFrom)
                .orElse(dailyDateTimeTo.minusMonths(1));
        List<Ohlcv> dailyOhlcvs = brokerAssetOhlcvRepository.findAllByBrokerIdAndAssetIdAndType(brokerId, assetId, Ohlcv.Type.DAILY, dailyDateTimeFrom, dailyDateTimeTo, Pageable.unpaged())
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());

        return Optional.ofNullable(AssetIndicator.builder()
                .assetId(assetId)
                .assetName(brokerAssetEntity.getAssetName())
                .minuteOhlcvs(minuteOhlcvs)
                .dailyOhlcvs(dailyOhlcvs)
                .build());
    }

//    public Page<Ohlcv> getBrokerAssetOhlcvs(String brokerId, String assetId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
//        Page<BrokerAssetOhlcvEntity> brokerAssetOhlcvPage = brokerAssetOhlcvRepository.findAllByBrokerIdAndAssetIdAndType(brokerId, assetId, type, dateTimeFrom, dateTimeTo, pageable);
//        List<Ohlcv> brokerAssetOhlcvs = brokerAssetOhlcvPage.getContent().stream()
//                .map(Ohlcv::from)
//                .collect(Collectors.toList());
//        long total = brokerAssetOhlcvPage.getTotalElements();
//        return new PageImpl<>(brokerAssetOhlcvs, pageable, total);
//    }

}
