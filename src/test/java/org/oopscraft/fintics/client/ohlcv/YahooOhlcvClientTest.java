package org.oopscraft.fintics.client.ohlcv;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class YahooOhlcvClientTest extends CoreTestSupport {

    private final ObjectMapper objectMapper;

    YahooOhlcvClient getYahooOhlcvClient() {
        return new YahooOhlcvClient(objectMapper);
    }

    void createAsset(Asset asset) {
        AssetEntity assetEntity = AssetEntity.builder()
                .assetId(asset.getAssetId())
                .exchange(asset.getExchange())
                .build();
        entityManager.persist(assetEntity);
        entityManager.flush();
    }

    @Disabled
    @Test
    void getAssetOhlcvs() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .exchange("XKRX")
                .build();
        createAsset(asset);
        LocalDateTime dateTimeTo = LocalDateTime.now().minusDays(1);
        LocalDateTime dateTimeFrom = dateTimeTo.minusWeeks(11);

        // when
        List<Ohlcv> ohlcvs = getYahooOhlcvClient().getAssetOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() > 0);
    }

    @Disabled
    @Test
    void getIndiceOhlcvs() {
        // given
        Indice indice = Indice.from(Indice.Id.NDX_FUTURE);
        LocalDateTime dateTimeTo = LocalDateTime.now().minusDays(1);
        LocalDateTime dateTimeFrom = dateTimeTo.minusWeeks(11);

        // when
        List<Ohlcv> ohlcvs = getYahooOhlcvClient().getIndiceOhlcvs(indice, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);

        // then
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() > 0);
    }

}