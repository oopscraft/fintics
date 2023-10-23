package org.oopscraft.fintics.thread;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class TradeAssetDeciderTest {

    @Test
    void test() {
        // given
        Trade trade = Trade.builder()
                .tradeId("test")
                .holdCondition("println tool.slope(assetIndicator.getMinutePrices(),1);")
                .build();
        TradeAsset tradeAsset = TradeAsset.builder()
                .tradeId("test")
                .name("Test")
                .build();

        // when
        TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                .trade(trade)
                .tradeAsset(tradeAsset)
                .assetIndicator(AssetIndicator.builder()
                        .asset(TradeAsset.builder()
                                .tradeId("test")
                                .symbol("test")
                                .type(AssetType.ETF)
                                .build())
                        .minuteOhlcvs(IntStream.range(1,11)
                                .mapToObj(i -> {
                                    double price = 1000 - (i*10);
                                    return Ohlcv.builder()
                                            .dateTime(LocalDateTime.now().minusMinutes(i))
                                            .openPrice(price)
                                            .highPrice(price)
                                            .lowPrice(price)
                                            .closePrice(price)
                                            .build();
                                })
                                .collect(Collectors.toList()))
                        .dailyOhlcvs(IntStream.range(1,11)
                                .mapToObj(i -> {
                                    double price = 1000 - (i*10);
                                    return Ohlcv.builder()
                                            .dateTime(LocalDateTime.now().minusDays(i))
                                            .openPrice(price)
                                            .highPrice(price)
                                            .lowPrice(price)
                                            .closePrice(price)
                                            .build();
                                })
                                .collect(Collectors.toList()))
                        .build())
                .build();
        Boolean result = tradeAssetDecider.execute();

        log.info("== result:{}", result);
    }

}