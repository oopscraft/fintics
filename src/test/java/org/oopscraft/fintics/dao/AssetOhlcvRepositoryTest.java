package org.oopscraft.fintics.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
class AssetOhlcvRepositoryTest extends CoreTestSupport {

    private final BrokerAssetOhlcvRepository brokerAssetOhlcvRepository;

    @Test
    void findMaxDateTimeBySymbolAndType() {
        // given
        String clientId = "KIS";
        String symbol = "122630";
        Ohlcv.Type type = Ohlcv.Type.MINUTE;

        // when
        LocalDateTime dateTime = brokerAssetOhlcvRepository.findMaxDateTimeByBrokerIdAndAssetIdAndType(clientId, symbol, type)
                .orElse(LocalDateTime.MIN);
        // then
        assertNotNull(dateTime);
    }


}