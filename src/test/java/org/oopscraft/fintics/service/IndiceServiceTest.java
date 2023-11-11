package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.IndiceIndicator;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class IndiceServiceTest extends CoreTestSupport {

    private final IndiceService indiceService;

    @Disabled
    @Test
    void getMarket() throws InterruptedException {
        // given
        // when
        List<IndiceIndicator> indiceIndicators = indiceService.getIndiceIndicators();

        // then
        assertTrue(indiceIndicators.size() > 0);
    }

}