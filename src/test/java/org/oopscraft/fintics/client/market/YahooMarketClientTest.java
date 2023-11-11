package org.oopscraft.fintics.client.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.client.indice.YahooIndiceClient;
import org.oopscraft.fintics.dao.IndiceOhlcvEntity;
import org.oopscraft.fintics.model.IndiceIndicator;
import org.oopscraft.fintics.model.IndiceSymbol;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
class YahooMarketClientTest extends CoreTestSupport {

    private final ObjectMapper objectMapper;

    @Disabled
    @Test
    void getMinuteOhlcvs() throws InterruptedException {
        // given
        // when
        IndiceClient indiceClient = new YahooIndiceClient(objectMapper);
        List<Ohlcv> minuteOhlcvs = indiceClient.getMinuteOhlcvs(IndiceSymbol.NDX);

        // then
        assertTrue(minuteOhlcvs.size() > 0);
    }

    @Disabled
    @Test
    void getDailyOhlcvs() throws InterruptedException {
        // given
        // when
        IndiceClient indiceClient = new YahooIndiceClient(objectMapper);
        List<Ohlcv> dailyOhlcvs = indiceClient.getDailyOhlcvs(IndiceSymbol.NDX);

        // then
        assertTrue(dailyOhlcvs.size() > 0);
    }

}