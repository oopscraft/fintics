package org.oopscraft.fintics.client.ohlcv.yahoo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.ohlcv.OhlcvClientProperties;
import org.oopscraft.fintics.client.ohlcv.yahoo.YahooOhlcvClient;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class YahooOhlcvClientTest extends CoreTestSupport {

    private final OhlcvClientProperties ohlcvClientProperties;

    private final ObjectMapper objectMapper;

    YahooOhlcvClient getYahooOhlcvClient() {
        return new YahooOhlcvClient(ohlcvClientProperties, objectMapper);
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
        boolean supported = getYahooOhlcvClient().isSupported(asset);
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
    void getAssetMinuteOhlcvsAfter30Days(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        saveAsset(asset);
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(30);
        LocalDateTime dateTimeTo = LocalDateTime.now();

        // when
        List<Ohlcv> ohlcvs = getYahooOhlcvClient().getAssetOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() >= 60 * 4 * 15);
        Ohlcv firstOhlcv = ohlcvs.get(0);
        Ohlcv lastOhlcv = ohlcvs.get(ohlcvs.size()-1);
        assertTrue(firstOhlcv.getDateTime().isBefore(dateTimeTo));
        assertTrue(lastOhlcv.getDateTime().isAfter(dateTimeFrom));
    }

    @ParameterizedTest
    @MethodSource("getAssetInfos")
    void getAssetMinuteOhlcvAcross30DaysAgo(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        saveAsset(asset);
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(60);
        LocalDateTime dateTimeTo = LocalDateTime.now().minusDays(15);

        // when
        List<Ohlcv> ohlcvs = getYahooOhlcvClient().getAssetOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() >= 60 * 4 * 7);
        Ohlcv firstOhlcv = ohlcvs.get(0);
        Ohlcv lastOhlcv = ohlcvs.get(ohlcvs.size()-1);
        assertTrue(firstOhlcv.getDateTime().isBefore(dateTimeTo));
        assertTrue(lastOhlcv.getDateTime().isAfter(dateTimeFrom));
    }

    @ParameterizedTest
    @MethodSource("getAssetInfos")
    void getAssetMinuteOhlcvsBefore30DaysAgo(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        saveAsset(asset);
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(60);
        LocalDateTime dateTimeTo = LocalDateTime.now().minusDays(30);

        // when
        List<Ohlcv> ohlcvs = getYahooOhlcvClient().getAssetOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() < 1);
    }

    @ParameterizedTest
    @MethodSource("getAssetInfos")
    void getAssetDailyOhlcvsAfter1YearsAgo(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        saveAsset(asset);
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusMonths(11);
        LocalDateTime dateTimeTo = LocalDateTime.now();

        // when
        List<Ohlcv> ohlcvs = getYahooOhlcvClient().getAssetOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() > 0);
    }

    @ParameterizedTest
    @MethodSource("getAssetInfos")
    void getAssetDailyOhlcvAcross1YearsAgo(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        saveAsset(asset);
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusYears(2);
        LocalDateTime dateTimeTo = LocalDateTime.now().minusMonths(6);

        // when
        List<Ohlcv> ohlcvs = getYahooOhlcvClient().getAssetOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo);

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
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusYears(2);
        LocalDateTime dateTimeTo = LocalDateTime.now().minusYears(1);

        // when
        List<Ohlcv> ohlcvs = getYahooOhlcvClient().getAssetOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() < 1);
    }

    static Stream<Arguments> getIndiceIds() {
        return Stream.of(
                Arguments.of(Indice.Id.NDX_FUTURE),
                Arguments.of(Indice.Id.KOSPI)
        );
    }

    @ParameterizedTest
    @MethodSource("getIndiceIds")
    void getIndiceMinuteOhlcvsAfter30Days(Indice.Id indiceId) {
        // given
        Indice indice = Indice.from(indiceId);
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(30);
        LocalDateTime dateTimeTo = LocalDateTime.now();

        // when
        List<Ohlcv> ohlcvs = getYahooOhlcvClient().getIndiceOhlcvs(indice, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() >= 60 * 4 * 15);
        Ohlcv firstOhlcv = ohlcvs.get(0);
        Ohlcv lastOhlcv = ohlcvs.get(ohlcvs.size()-1);
        assertTrue(firstOhlcv.getDateTime().isBefore(dateTimeTo));
        assertTrue(lastOhlcv.getDateTime().isAfter(dateTimeFrom));
    }


}