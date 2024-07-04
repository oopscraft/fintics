package org.oopscraft.fintics.client.ohlcv;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.model.Asset;
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
class SimpleOhlcvClientTest extends CoreTestSupport {

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
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(30);
        LocalDateTime dateTimeTo = LocalDateTime.now();

        // when
        List<Ohlcv> ohlcvs = getSimpleOhlcvClient().getOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);

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
    void getDailyOhlcvs(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        saveAsset(asset);
        LocalDateTime dateTimeFrom = LocalDateTime.now()
                .minusMonths(11);
        LocalDateTime dateTimeTo = LocalDateTime.now();

        // when
        List<Ohlcv> ohlcvs = getSimpleOhlcvClient().getOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() > 0);
    }

}