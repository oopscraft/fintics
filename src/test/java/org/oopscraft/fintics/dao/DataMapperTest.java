package org.oopscraft.fintics.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.OhlcvSummary;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
public class DataMapperTest extends CoreTestSupport {

    private final DataMapper dataMapper;

    @Test
    void selectAssets() {
        // given
        String assetId = "test";
        String assetName = "name";
        String market = "market";
        entityManager.persist(AssetEntity.builder()
                .assetId(assetId)
                .assetName(assetName)
                .market(market)
                .build());
        entityManager.flush();
        // when
        List<Asset> assets = dataMapper.selectAssets(assetId, assetName, market, new RowBounds(0, 10));
        // then
        log.info("assets: {}", assets);
        assertTrue(assets.size() > 0);
    }

    @Test
    void selectOhlcvSummaries() {
        // given
        String assetId = "US.test";
        entityManager.persist(OhlcvEntity.builder()
                .assetId(assetId)
                .type(Ohlcv.Type.MINUTE)
                .dateTime(LocalDateTime.now())
                .build());
        entityManager.flush();
        // when
        List<OhlcvSummary> ohlcvSummaries = dataMapper.selectOhlcvSummaries(assetId);
        // then
        log.info("ohlcvSummaries: {}", ohlcvSummaries);
        assertTrue(ohlcvSummaries.size() > 0);
    }

}
