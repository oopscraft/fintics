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
class RuleScriptExecutorTest {

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

    OrderBook getTestOrderBook() {
        return OrderBook.builder()
                .price(BigDecimal.valueOf(10000))
                .bidPrice(BigDecimal.valueOf(9990))
                .askPrice(BigDecimal.valueOf(10010))
                .build();
    }

    List<IndiceIndicator> getTestIndiceIndicators() {
        List<IndiceIndicator> indiceIndicators = new ArrayList<>();
        for(IndiceId symbol : IndiceId.values()) {
            indiceIndicators.add(IndiceIndicator.builder()
                    .indiceId(symbol)
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
        return indiceIndicators;
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

    AssetIndicator getTestAssetIndicator(TradeAsset tradeAsset) {
        return AssetIndicator.builder()
                .assetId(tradeAsset.getAssetId())
                .assetName(tradeAsset.getAssetName())
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

    @Test
    void test() {
        // given
        Trade trade = getTestTrade();
        TradeAsset tradeAsset = getTestTradeAsset();
        OrderBook orderBook = getTestOrderBook();
        List<IndiceIndicator> indiceIndicators = getTestIndiceIndicators();
        AssetIndicator assetIndicator = getTestAssetIndicator(tradeAsset);
        trade.setRuleConfig("");
        trade.setRuleScript("return 50;");

        // when
        RuleScriptExecutor tradeAssetDecider = RuleScriptExecutor.builder()
                .ruleConfig(trade.getRuleConfig())
                .ruleScript(trade.getRuleScript())
                .dateTime(LocalDateTime.now())
                .orderBook(orderBook)
                .indiceIndicators(indiceIndicators)
                .assetIndicator(assetIndicator)
                .build();
        BigDecimal result = tradeAssetDecider.execute();

        // then
        log.info("== result:{}", result);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(50)));
    }

    @Test
    void testRuleScript() {
        // given
        Trade trade = getTestTrade();
        TradeAsset tradeAsset = getTestTradeAsset();
        List<IndiceIndicator> indiceIndicators = getTestIndiceIndicators();
        AssetIndicator assetIndicator = getTestAssetIndicator(tradeAsset);
        StringBuilder ruleConfig = new StringBuilder();
        ruleConfig.append("waveOhlcvType=MINUTE").append("\n");
        ruleConfig.append("waveOhlcvPeriod=3").append("\n");
        ruleConfig.append("tideOhlcvType=DAILY").append("\n");
        ruleConfig.append("tideOhlcvPeriod=1").append("\n");
        String ruleScript = loadGroovyFileAsString("RuleScript.groovy");
        trade.setRuleConfig(ruleConfig.toString());
        trade.setRuleScript(ruleScript);

        // when
        RuleScriptExecutor tradeAssetDecider = RuleScriptExecutor.builder()
                .ruleConfig(trade.getRuleConfig())
                .ruleScript(trade.getRuleScript())
                .dateTime(LocalDateTime.now())
                .balance(new Balance())
                .indiceIndicators(indiceIndicators)
                .assetIndicator(assetIndicator)
                .build();
        BigDecimal result = tradeAssetDecider.execute();

        // then
        log.info("== result:{}", result);
    }

}