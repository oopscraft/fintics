package org.oopscraft.fintics.client.ohlcv.alphavantage;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.ohlcv.OhlcvClientProperties;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class AlphavantageOhlcvClientTest extends CoreTestSupport {

    private final OhlcvClientProperties ohlcvClientProperties;

    private final ObjectMapper objectMapper;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("fintics.ohlcv-client.properties.apikey", () -> System.getenv("ALPHAVANTAGE_APIKEY"));
    }

    AlphavantageOhlcvClient getAlphavantageOhlcvClient() {
        return new AlphavantageOhlcvClient(ohlcvClientProperties, objectMapper);
    }

    @Test
    void convertCsvStringToOhlcvs() {
        // given
        String csvString = "timestamp,open,high,low,close,volume\n" +
                "2024-05-07 19:55:00,168.5300,168.5500,168.5300,168.5500,14\n" +
                "2024-05-07 19:50:00,168.5300,168.5400,168.3900,168.3900,26\n" +
                "2024-05-07 19:35:00,168.3900,168.5300,168.3900,168.5300,2\n";
        // when
        List<Ohlcv> ohlcvs = getAlphavantageOhlcvClient().convertCsvStringToOhlcvs(csvString, Ohlcv.Type.MINUTE);
        // then
        assertTrue(ohlcvs.size() > 0);
    }

    @Disabled
    @Test
    void getMinuteOhlcvsByYearMonth() {
        // given
        String alphavantageSymbol = "AAPL";
        YearMonth yearMonth = YearMonth.of(2024, 4);
        // when
        List<Ohlcv> ohlcvs = getAlphavantageOhlcvClient().getMinuteOhlcvsByYearMonth(alphavantageSymbol, yearMonth);
        // then
        assertTrue(ohlcvs.size() > 0);
    }

    @Disabled
    @Test
    void getMinuteOhlcvs() {
        // given
        String alphavantageSymbol = "AAPL";
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(35);
        LocalDateTime dateTimeTo = LocalDateTime.now();
        // when
        List<Ohlcv> minuteOhlcvs = getAlphavantageOhlcvClient().getMinuteOhlcvs(alphavantageSymbol, dateTimeFrom, dateTimeTo);
        // then
        log.info("minuteOhlcvs.size(): {}", minuteOhlcvs.size());
    }

    @Disabled
    @Test
    void getDailyOhlcvs() {
        // given
        String alphavantageSymbol = "AAPL";
        // when
        List<Ohlcv> ohlcvs = getAlphavantageOhlcvClient().getDailyOhlcvs(alphavantageSymbol);
        // then
        assertTrue(ohlcvs.size() > 0);
    }

    @Test
    void getMonthsBetween() {
        // given
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusMonths(3);
        LocalDateTime dateTimeTo = LocalDateTime.now();
        // when
        List<YearMonth> yearMonths = getAlphavantageOhlcvClient().getYearMonthsBetween(dateTimeFrom, dateTimeTo);
        // then
        log.info("yearMonths: {}", yearMonths);
    }

    @Test
    void getAssetOhlcvs() {
        // given
        Asset asset = Asset.builder()
                .assetId("")
                .exchange("")
                .build();
        Ohlcv.Type type = Ohlcv.Type.MINUTE;
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(12);
        LocalDateTime dateTimeTo = LocalDateTime.now();
        // when
        List<Ohlcv> ohlcvs = getAlphavantageOhlcvClient().getAssetOhlcvs(asset, type, dateTimeFrom, dateTimeTo);
        // then
        log.info("ohlcvs.size(): {}", ohlcvs.size());
    }








//    void saveAsset(Asset asset) {
//        AssetEntity assetEntity = entityManager.find(AssetEntity.class, asset.getAssetId());
//        if (assetEntity == null) {
//            assetEntity = AssetEntity.builder()
//                    .assetId(asset.getAssetId())
//                    .build();
//        }
//        assetEntity.setExchange(asset.getExchange());
//        entityManager.persist(assetEntity);
//        entityManager.flush();
//    }
//
//    static Stream<Arguments> getIsSupportedArguments() {
//        return Stream.of(
//                Arguments.of("US.IBM", "XNYS", true)
//        );
//    }
//
//    @ParameterizedTest
//    @MethodSource("getIsSupportedArguments")
//    void isSupported(String assetId, String exchange, boolean expected) {
//        // given
//        Asset asset = Asset.builder()
//                .assetId(assetId)
//                .exchange(exchange)
//                .build();
//        saveAsset(asset);
//        // when
//        boolean supported = getAlphavantageOhlcvClient().isSupported(asset);
//        // then
//        assertEquals(expected, supported);
//    }
//
//    static Stream<Arguments> getAssetInfos() {
//        return Stream.of(
//                Arguments.of("US.IBM", "XNYS")
//        );
//    }
//
//    @ParameterizedTest
//    @MethodSource("getAssetInfos")
//    void getAssetMinuteOhlcvs(String assetId, String exchange) {
//        // given
//        Asset asset = Asset.builder()
//                .assetId(assetId)
//                .exchange(exchange)
//                .build();
//        saveAsset(asset);
//        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(30);
//        LocalDateTime dateTimeTo = LocalDateTime.now();
//
//        // when
//        List<Ohlcv> ohlcvs = getAlphavantageOhlcvClient().getAssetOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);
//
//        // then
//        log.debug("ohlcvs.size():{}", ohlcvs.size());
//        assertTrue(ohlcvs.size() >= 60 * 4 * 15);
//        Ohlcv firstOhlcv = ohlcvs.get(0);
//        Ohlcv lastOhlcv = ohlcvs.get(ohlcvs.size()-1);
//        assertTrue(firstOhlcv.getDateTime().isBefore(dateTimeTo));
//        assertTrue(lastOhlcv.getDateTime().isAfter(dateTimeFrom));
//    }
//
//    @ParameterizedTest
//    @MethodSource("getAssetInfos")
//    void getAssetMinuteOhlcvAcross30DaysAgo(String assetId, String exchange) {
//        // given
//        Asset asset = Asset.builder()
//                .assetId(assetId)
//                .exchange(exchange)
//                .build();
//        saveAsset(asset);
//        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(60);
//        LocalDateTime dateTimeTo = LocalDateTime.now().minusDays(15);
//
//        // when
//        List<Ohlcv> ohlcvs = getAlphavantageOhlcvClient().getAssetOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);
//
//        // then
//        log.debug("ohlcvs.size():{}", ohlcvs.size());
//        assertTrue(ohlcvs.size() >= 60 * 4 * 7);
//        Ohlcv firstOhlcv = ohlcvs.get(0);
//        Ohlcv lastOhlcv = ohlcvs.get(ohlcvs.size()-1);
//        assertTrue(firstOhlcv.getDateTime().isBefore(dateTimeTo));
//        assertTrue(lastOhlcv.getDateTime().isAfter(dateTimeFrom));
//    }
//
//    @ParameterizedTest
//    @MethodSource("getAssetInfos")
//    void getAssetMinuteOhlcvsBefore30DaysAgo(String assetId, String exchange) {
//        // given
//        Asset asset = Asset.builder()
//                .assetId(assetId)
//                .exchange(exchange)
//                .build();
//        saveAsset(asset);
//        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(60);
//        LocalDateTime dateTimeTo = LocalDateTime.now().minusDays(30);
//
//        // when
//        List<Ohlcv> ohlcvs = getAlphavantageOhlcvClient().getAssetOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);
//
//        // then
//        log.debug("ohlcvs.size():{}", ohlcvs.size());
//        assertTrue(ohlcvs.size() < 1);
//    }
//
//    @ParameterizedTest
//    @MethodSource("getAssetInfos")
//    void getAssetDailyOhlcvsAfter1YearsAgo(String assetId, String exchange) {
//        // given
//        Asset asset = Asset.builder()
//                .assetId(assetId)
//                .exchange(exchange)
//                .build();
//        saveAsset(asset);
//        LocalDateTime dateTimeFrom = LocalDateTime.now().minusMonths(11);
//        LocalDateTime dateTimeTo = LocalDateTime.now();
//
//        // when
//        List<Ohlcv> ohlcvs = getAlphavantageOhlcvClient().getAssetOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo);
//
//        // then
//        log.debug("ohlcvs.size():{}", ohlcvs.size());
//        assertTrue(ohlcvs.size() > 0);
//    }
//
//    @ParameterizedTest
//    @MethodSource("getAssetInfos")
//    void getAssetDailyOhlcvAcross1YearsAgo(String assetId, String exchange) {
//        // given
//        Asset asset = Asset.builder()
//                .assetId(assetId)
//                .exchange(exchange)
//                .build();
//        saveAsset(asset);
//        LocalDateTime dateTimeFrom = LocalDateTime.now().minusYears(2);
//        LocalDateTime dateTimeTo = LocalDateTime.now().minusMonths(6);
//
//        // when
//        List<Ohlcv> ohlcvs = getAlphavantageOhlcvClient().getAssetOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo);
//
//        // then
//        log.debug("ohlcvs.size():{}", ohlcvs.size());
//        assertTrue(ohlcvs.size() > 0);
//    }
//
//    @ParameterizedTest
//    @MethodSource("getAssetInfos")
//    void getAssetDailyOhlcvsBefore1YearsAgo(String assetId, String exchange) {
//        // given
//        Asset asset = Asset.builder()
//                .assetId(assetId)
//                .exchange(exchange)
//                .build();
//        saveAsset(asset);
//        LocalDateTime dateTimeFrom = LocalDateTime.now().minusYears(2);
//        LocalDateTime dateTimeTo = LocalDateTime.now().minusYears(1);
//
//        // when
//        List<Ohlcv> ohlcvs = getAlphavantageOhlcvClient().getAssetOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo);
//
//        // then
//        log.debug("ohlcvs.size():{}", ohlcvs.size());
//        assertTrue(ohlcvs.size() < 1);
//    }
//
//    static Stream<Arguments> getIndiceIds() {
//        return Stream.of(
//                Arguments.of(Indice.Id.NDX_FUTURE),
//                Arguments.of(Indice.Id.KOSPI)
//        );
//    }
//
//    @ParameterizedTest
//    @MethodSource("getIndiceIds")
//    void getIndiceMinuteOhlcvsAfter30Days(Indice.Id indiceId) {
//        // given
//        Indice indice = Indice.from(indiceId);
//        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(30);
//        LocalDateTime dateTimeTo = LocalDateTime.now();
//
//        // when
//        List<Ohlcv> ohlcvs = getAlphavantageOhlcvClient().getIndiceOhlcvs(indice, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);
//
//        // then
//        log.debug("ohlcvs.size():{}", ohlcvs.size());
//        assertTrue(ohlcvs.size() >= 60 * 4 * 15);
//        Ohlcv firstOhlcv = ohlcvs.get(0);
//        Ohlcv lastOhlcv = ohlcvs.get(ohlcvs.size()-1);
//        assertTrue(firstOhlcv.getDateTime().isBefore(dateTimeTo));
//        assertTrue(lastOhlcv.getDateTime().isAfter(dateTimeFrom));
//    }

}