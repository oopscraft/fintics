package org.oopscraft.fintics.rule;

import com.github.javaparser.utils.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.rule.TradeAssetDecider;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class TradeAssetDeciderTest {

    Trade getTestTrade() {
        return Trade.builder()
                .tradeId("test")
                .build();
    }

    TradeAsset getTestTradeAsset() {
        return TradeAsset.builder()
                .tradeId("test")
                .name("Test")
                .build();
    }

    List<IndiceIndicator> getTestIndiceIndicators() {
        return new ArrayList<IndiceIndicator>();
    }

    String loadGroovyFileAsString(String fileName) {
        String filePath = "org/oopscraft/fintics/rule/" + fileName;
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            IOUtils.readLines(inputStream, StandardCharsets.UTF_8).forEach(line -> {
                stringBuilder.append(line).append(LineSeparator.LF);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }

    AssetIndicator getTestAssetIndicator(TradeAsset tradeAsset) {
        return AssetIndicator.builder()
                .symbol(tradeAsset.getSymbol())
                .name(tradeAsset.getName())
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
        List<IndiceIndicator> indiceIndicators = getTestIndiceIndicators();
        AssetIndicator assetIndicator = getTestAssetIndicator(tradeAsset);
        trade.setHoldCondition("return true;");

        // when
        TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                .holdCondition(trade.getHoldCondition())
                .logger(log)
                .dateTime(LocalDateTime.now())
                .assetIndicator(assetIndicator)
                .indiceIndicators(indiceIndicators)
                .build();
        Boolean result = tradeAssetDecider.execute();

        // then
        log.info("== result:{}", result);
        assertTrue(result);
    }

    @Test
    void testHoldConditionCall() {
        // given
        Trade trade = getTestTrade();
        TradeAsset tradeAsset = getTestTradeAsset();
        List<IndiceIndicator> indiceIndicators = getTestIndiceIndicators();
        AssetIndicator assetIndicator = getTestAssetIndicator(tradeAsset);
        String holdCondition = loadGroovyFileAsString("HoldConditionCall.groovy");
        trade.setHoldCondition(holdCondition);

        // when
        TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                .holdCondition(trade.getHoldCondition())
                .logger(log)
                .dateTime(LocalDateTime.now())
                .assetIndicator(assetIndicator)
                .indiceIndicators(indiceIndicators)
                .build();
        Boolean result = tradeAssetDecider.execute();

        // then
        log.info("== result:{}", result);
    }

}