package org.oopscraft.fintics.client.indice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.IndiceSymbol;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class YahooIndiceClientTest extends CoreTestSupport {

    private final ObjectMapper objectMapper;

    @Disabled
    @Test
    void getMinuteOhlcvs() {
        // given
        // when
        YahooIndiceClient indiceClient = new YahooIndiceClient(objectMapper);
        for(IndiceSymbol indiceSymbol : IndiceSymbol.values()) {
            log.info("====== indiceSymbol[{}] =====", indiceSymbol);
            List<Ohlcv> ohlcvs = indiceClient.getMinuteOhlcvs(indiceSymbol);
            log.debug("ohlcvs:{}", ohlcvs);
        }
        // then
    }

    @Test
    void getDailyOhlcvs() {
    }
}