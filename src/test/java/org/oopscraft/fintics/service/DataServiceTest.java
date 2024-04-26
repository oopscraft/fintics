package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.support.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
class DataServiceTest extends CoreTestSupport {

    private final DataService dataService;

    private final EntityManager entityManager;

    @Test
    @Disabled
    void interpolateAssetOhlcvs() {
        // given
        String assetId = "KR.005930";
        Ohlcv.Type type = Ohlcv.Type.MINUTE;
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusMonths(3);
        LocalDateTime dateTimeTo = dateTimeFrom.plusHours(1);

        // when
        dataService.interpolateAssetOhlcvs(assetId, type, dateTimeFrom, dateTimeTo);

        // then
    }

    @Test
    @Disabled
    void interpolateIndiceOhlcvs() {

    }

}