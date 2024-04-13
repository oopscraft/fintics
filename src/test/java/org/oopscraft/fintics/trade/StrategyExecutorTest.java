package org.oopscraft.fintics.trade;

import com.github.javaparser.utils.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class StrategyExecutorTest {

    Trade getTestTrade() {
        return Trade.builder()
                .tradeId("test")
                .build();
    }

    TradeAsset getTestTradeAsset() {
        return TradeAsset.builder()
                .tradeId("test")
                .assetName("Test")
                .build();
    }

//    List<IndiceProfile> getTestIndices() {
//        List<IndiceProfile> indiceProfiles = new ArrayList<>();
//        for (Indice.Id indiceId : Indice.Id.values()) {
//            IndiceProfile indiceProfile = IndiceProfile.builder()
//                    .minuteOhlcvs(new ArrayList<>())
//                    .dailyOhlcvs(new ArrayList<>())
//                    .build();
//            indiceProfiles.add(indiceProfile);
//        }
//        return indiceProfiles;
//    }
//
//    AssetProfile getTestAssetProfile() {
//        return AssetProfile.builder()
//                .minuteOhlcvs(new ArrayList<>())
//                .dailyOhlcvs(new ArrayList<>())
//                .build();
//    }

    OrderBook getTestOrderBook() {
        return OrderBook.builder()
                .price(BigDecimal.valueOf(10000))
                .bidPrice(BigDecimal.valueOf(9990))
                .askPrice(BigDecimal.valueOf(10010))
                .build();
    }

    List<IndiceProfile> getTestIndiceProfiles() {
        List<IndiceProfile> indiceProfiles = new ArrayList<>();
        for(Indice.Id indiceId : Indice.Id.values()) {
            Indice indice = Indice.from(indiceId);
            indiceProfiles.add(IndiceProfile.builder()
                    .target(indice)
                    .minuteOhlcvs(new ArrayList<Ohlcv>(){{
                        add(Ohlcv.builder()
                                .dateTime(LocalDateTime.now())
                                .openPrice(BigDecimal.TEN)
                                .highPrice(BigDecimal.TEN)
                                .lowPrice(BigDecimal.TEN)
                                .closePrice(BigDecimal.TEN)
                                .volume(BigDecimal.TEN)
                                .build());
                    }})
                    .dailyOhlcvs(new ArrayList<Ohlcv>(){{
                        add(Ohlcv.builder()
                                .dateTime(LocalDate.now().atTime(0,0,0))
                                .openPrice(BigDecimal.TEN)
                                .highPrice(BigDecimal.TEN)
                                .lowPrice(BigDecimal.TEN)
                                .closePrice(BigDecimal.TEN)
                                .volume(BigDecimal.TEN)
                                .build());
                    }})
                    .build());
        }
        return indiceProfiles;
    }

    AssetProfile getTestAssetProfile(TradeAsset tradeAsset) {
        return AssetProfile.builder()
                .target(tradeAsset)
                .minuteOhlcvs(IntStream.range(1,501)
                        .mapToObj(i -> {
                            BigDecimal price = BigDecimal.valueOf(1000 - (i*10));
                            return Ohlcv.builder()
                                    .dateTime(LocalDateTime.now().minusMinutes(i))
                                    .openPrice(price)
                                    .highPrice(price)
                                    .lowPrice(price)
                                    .closePrice(price)
                                    .volume(BigDecimal.valueOf(100))
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .dailyOhlcvs(IntStream.range(1,301)
                        .mapToObj(i -> {
                            BigDecimal price = BigDecimal.valueOf(1000 - (i*10));
                            return Ohlcv.builder()
                                    .dateTime(LocalDateTime.now().minusDays(i))
                                    .openPrice(price)
                                    .highPrice(price)
                                    .lowPrice(price)
                                    .closePrice(price)
                                    .volume(BigDecimal.valueOf(10000))
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .build();
    }

    String loadGroovyFileAsString(String fileName) {
        String filePath = null;
        try {
            filePath = new File(".").getCanonicalPath() + "/src/main/groovy/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = new FileInputStream(new File(filePath))) {
            IOUtils.readLines(inputStream, StandardCharsets.UTF_8).forEach(line -> {
                stringBuilder.append(line).append(LineSeparator.LF);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }

    @Test
    void test() {
        // given
        Trade trade = getTestTrade();
        TradeAsset tradeAsset = getTestTradeAsset();
        List<IndiceProfile> indiceProfiles = getTestIndiceProfiles();
        AssetProfile assetProfile = getTestAssetProfile(tradeAsset);
        OrderBook orderBook = getTestOrderBook();
        trade.setStrategyVariables("");
        Strategy strategy = Strategy.builder()
                .script("return 1")
                .build();

        // when
        StrategyExecutor tradeAssetDecider = StrategyExecutor.builder()
                .indiceProfiles(indiceProfiles)
                .assetProfile(assetProfile)
                .strategy(strategy)
                .dateTime(LocalDateTime.now())
                .orderBook(orderBook)
                .build();
        BigDecimal result = tradeAssetDecider.execute();

        // then
        log.info("== result:{}", result);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(1)));
    }

    @Test
    void testStrategyScript() {
        // given
        Trade trade = getTestTrade();
        TradeAsset tradeAsset = getTestTradeAsset();
        List<IndiceProfile> indiceProfiles = getTestIndiceProfiles();
        AssetProfile assetProfile = getTestAssetProfile(tradeAsset);
        String strategyScript = loadGroovyFileAsString("StrategyScript.groovy");
        Strategy strategy = Strategy.builder()
                        .script(strategyScript)
                        .build();
        StringBuilder strategyVariables = new StringBuilder();
        strategyVariables.append("waveOhlcvType=MINUTE").append("\n");
        strategyVariables.append("waveOhlcvPeriod=3").append("\n");
        strategyVariables.append("tideOhlcvType=DAILY").append("\n");
        strategyVariables.append("tideOhlcvPeriod=1").append("\n");

        // when
        StrategyExecutor strategyExecutor = StrategyExecutor.builder()
                .indiceProfiles(indiceProfiles)
                .assetProfile(assetProfile)
                .strategy(strategy)
                .variables(strategyVariables.toString())
                .dateTime(LocalDateTime.now())
                .balance(new Balance())
                .build();
        BigDecimal result = strategyExecutor.execute();

        // then
        log.info("== result:{}", result);
    }

}