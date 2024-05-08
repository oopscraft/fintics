package org.oopscraft.fintics.client.ohlcv.alphavantage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.ohlcv.OhlcvClientProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
class AlphavantageOhlcvCollectorTest extends CoreTestSupport {

    private final OhlcvClientProperties ohlcvClientProperties;

    private final AlphavantageOhlcvRepository alphavantageOhlcvRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("fintics.ohlcv-client.properties.apikey", () -> System.getenv("ALPHAVANTAGE_APIKEY"));
    }

    AlphavantageOhlcvCollector getAlphavantageOhlcvCollector() {
        return new AlphavantageOhlcvCollector(ohlcvClientProperties, alphavantageOhlcvRepository);
    }

//    @Disabled
//    @Test
//    void collect() {
//        // given
//        String symbol = "IBM";
//        String interval = "5min";
//        String month = "2009-01";
//        // when
//        getAlphavantageOhlcvCollector().collect(symbol, interval, month);
//        // then
//    }

}