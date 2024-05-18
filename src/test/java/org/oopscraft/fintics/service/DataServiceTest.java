package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.OhlcvSummary;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class DataServiceTest extends CoreTestSupport {

    private final DataService dataService;

    private final EntityManager entityManager;

    @Test
    void getAssets() {
        // given
        // when
        List<Asset> assets = dataService.getAssets(null, null, null, PageRequest.of(0, 10));
        // then
        log.info("assets: {}", assets);
    }

    @Test
    void getAssetOhlcvSummaries() {
        // given
        // when
        List<OhlcvSummary> assetOhlcvSummaries = dataService.getAssetOhlcvSummaries();
        // then
        log.info("assetOhlcvSummaries: {}", assetOhlcvSummaries);
    }

}