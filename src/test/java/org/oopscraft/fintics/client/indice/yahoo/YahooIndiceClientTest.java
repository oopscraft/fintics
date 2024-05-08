package org.oopscraft.fintics.client.indice.yahoo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.indice.IndiceClientProperties;
import org.oopscraft.fintics.client.indice.yahoo.YahooIndiceClient;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class YahooIndiceClientTest extends CoreTestSupport {

    private final IndiceClientProperties indiceClientProperties;

    private final ObjectMapper objectMapper;

    YahooIndiceClient getYahooIndiceClient() {
        return new YahooIndiceClient(indiceClientProperties, objectMapper);
    }

    @Disabled
    @Test
    void getMinuteOhlcvs() {
        // given
        // when
        YahooIndiceClient indiceClient = getYahooIndiceClient();
        for(Indice.Id indiceId : Indice.Id.values()) {
            log.info("====== indice[{}] =====", indiceId);
            List<Ohlcv> ohlcvs = indiceClient.getMinuteOhlcvs(indiceId, LocalDateTime.now());
            log.debug("indiceOhlcvs:{}", ohlcvs);
        }
        // then
    }

    @Disabled
    @Test
    void getDailyOhlcvs() {
        // given
        // when
        YahooIndiceClient indiceClient = getYahooIndiceClient();
        for(Indice.Id indiceId : Indice.Id.values()) {
            log.info("====== indice[{}] =====", indiceId);
            List<Ohlcv> ohlcvs = indiceClient.getDailyOhlcvs(indiceId, LocalDateTime.now());
            log.debug("indiceOhlcvs:{}", ohlcvs);
        }
        // then
    }
}