package org.oopscraft.fintics.client.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.ohlcv.OhlcvClientProperties;
import org.oopscraft.fintics.client.ohlcv.SimpleOhlcvClient;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class SimpleAssetOhlcvClientTest extends CoreTestSupport {

    private final OhlcvClientProperties ohlcvClientProperties;

    private final ObjectMapper objectMapper;

    SimpleOhlcvClient getSimpleOhlcvClient() {
        return new SimpleOhlcvClient(ohlcvClientProperties, objectMapper);
    }

    void saveAsset(Asset asset) {
        AssetEntity assetEntity = entityManager.find(AssetEntity.class, asset.getAssetId());
        if (assetEntity == null) {
            assetEntity = AssetEntity.builder()
                    .assetId(asset.getAssetId())
                    .build();
        }
        assetEntity.setExchange(asset.getExchange());
        entityManager.persist(assetEntity);
        entityManager.flush();
    }

    static Stream<Arguments> getIsSupportedArguments() {
        return Stream.of(
                Arguments.of("US.AAPL", "XNAS", true),
                Arguments.of("US.INVALIDXXX", "XNAS", false),
                Arguments.of("KR.005930", "XKRX", true),      // samsung electronics
                Arguments.of("KR.122630", "XKRX", true)       // KODEX Leverage ETF
        );
    }

    @ParameterizedTest
    @MethodSource("getIsSupportedArguments")
    void isSupported(String assetId, String exchange, boolean expected) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        saveAsset(asset);
        // when
        boolean supported = getSimpleOhlcvClient().isSupported(asset);
        // then
        assertEquals(expected, supported);
    }

    static Stream<Arguments> getAssetInfos() {
        return Stream.of(
                Arguments.of("KR.005930", "XKRX"),      // samsung electronics
                Arguments.of("KR.122630", "XKRX"),      // KODEX Leverage ETF
                Arguments.of("US.AAPL", "XNAS"),        // Apple
                Arguments.of("US.SPY", "XASE")          // SPY ETF
        );
    }

    @ParameterizedTest
    @MethodSource("getAssetInfos")
    void getMinuteOhlcvsAfter30Days(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        saveAsset(asset);
        Instant datetimeFrom = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant datetimeTo = Instant.now();

        // when
        List<Ohlcv> ohlcvs = getSimpleOhlcvClient().getOhlcvs(asset, Ohlcv.Type.MINUTE, datetimeFrom, datetimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() >= 60 * 4 * 15);
        Ohlcv firstOhlcv = ohlcvs.get(0);
        Ohlcv lastOhlcv = ohlcvs.get(ohlcvs.size()-1);
        assertTrue(firstOhlcv.getDatetime().isBefore(datetimeTo));
        assertTrue(lastOhlcv.getDatetime().isAfter(datetimeFrom));
    }

    @ParameterizedTest
    @MethodSource("getAssetInfos")
    void getMinuteOhlcvAcross30DaysAgo(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        saveAsset(asset);
        Instant datetimeFrom = Instant.now().minus(60, ChronoUnit.DAYS);
        Instant dateTimeTo = Instant.now().minus(15, ChronoUnit.DAYS);

        // when
        List<Ohlcv> ohlcvs = getSimpleOhlcvClient().getOhlcvs(asset, Ohlcv.Type.MINUTE, datetimeFrom, dateTimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() >= 60 * 4 * 7);
        Ohlcv firstOhlcv = ohlcvs.get(0);
        Ohlcv lastOhlcv = ohlcvs.get(ohlcvs.size()-1);
        assertTrue(firstOhlcv.getDatetime().isBefore(dateTimeTo));
        assertTrue(lastOhlcv.getDatetime().isAfter(datetimeFrom));
    }

    @ParameterizedTest
    @MethodSource("getAssetInfos")
    void getMinuteOhlcvsBefore30DaysAgo(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        saveAsset(asset);
        Instant datetimeFrom = Instant.now().minus(60, ChronoUnit.DAYS);
        Instant datetimeTo = Instant.now().minus(30, ChronoUnit.DAYS);
        // when
        List<Ohlcv> ohlcvs = getSimpleOhlcvClient().getOhlcvs(asset, Ohlcv.Type.MINUTE, datetimeFrom, datetimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() < 1);
    }

    @ParameterizedTest
    @MethodSource("getAssetInfos")
    void getDailyOhlcvsAfter1YearsAgo(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        saveAsset(asset);
        Instant datetimeFrom = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
                .minusMonths(11)
                .toInstant(ZoneOffset.UTC);
        Instant datetimeTo = Instant.now();

        // when
        List<Ohlcv> ohlcvs = getSimpleOhlcvClient().getOhlcvs(asset, Ohlcv.Type.DAILY, datetimeFrom, datetimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() > 0);
    }

    @ParameterizedTest
    @MethodSource("getAssetInfos")
    void getDailyOhlcvAcross1YearsAgo(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        saveAsset(asset);
        Instant datetimeFrom = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
                .minusYears(2)
                .toInstant(ZoneOffset.UTC);
        Instant datetimeTo = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
                .minusMonths(6)
                .toInstant(ZoneOffset.UTC);

        // when
        List<Ohlcv> ohlcvs = getSimpleOhlcvClient().getOhlcvs(asset, Ohlcv.Type.DAILY, datetimeFrom, datetimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() > 0);
    }

    @ParameterizedTest
    @MethodSource("getAssetInfos")
    void getAssetDailyOhlcvsBefore1YearsAgo(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        saveAsset(asset);
        Instant datetimeFrom = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
                .minusYears(2)
                .toInstant(ZoneOffset.UTC);
        Instant datetimeTo = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
                .minusYears(1)
                .toInstant(ZoneOffset.UTC);

        // when
        List<Ohlcv> ohlcvs = getSimpleOhlcvClient().getOhlcvs(asset, Ohlcv.Type.DAILY, datetimeFrom, datetimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() < 1);
    }

}