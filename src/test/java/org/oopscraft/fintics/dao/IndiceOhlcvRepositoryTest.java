package org.oopscraft.fintics.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsApplication.class)
@RequiredArgsConstructor
class IndiceOhlcvRepositoryTest extends CoreTestSupport {

    private final IndiceOhlcvRepository indiceOhlcvRepository;


}