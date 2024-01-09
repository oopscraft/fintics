package org.oopscraft.fintics.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.OhlcvType;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
class AssetOhlcvRepositoryTest extends CoreTestSupport {

    private final AssetOhlcvRepository assetOhlcvRepository;

    @Test
    void findMaxDateTimeBySymbolAndOhlcvType() {
        // given
        String clientId = "KIS";
        String symbol = "122630";
        OhlcvType ohlcvType = OhlcvType.MINUTE;

        // when
        LocalDateTime dateTime = assetOhlcvRepository.findMaxDateTimeBySymbolAndOhlcvType(clientId, symbol, ohlcvType)
                .orElse(LocalDateTime.MIN);
        // then
        assertNotNull(dateTime);
    }


}