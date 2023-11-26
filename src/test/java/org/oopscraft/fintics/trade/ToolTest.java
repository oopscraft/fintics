package org.oopscraft.fintics.trade;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.calculator._legacy.Dmi;
import org.oopscraft.fintics.calculator._legacy.Macd;
import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ToolTest {

    @SuperBuilder
    @Setter
    @Getter
    public static class FileOhlcv extends Ohlcv {
        private Macd macd;
        private BigDecimal rsi;
        private Dmi dmi;
    }

    @Test
    void testChart() {
        // given
        List<BigDecimal> rows = new ArrayList<>();
        for(int i = 0; i < 100; i ++) {
            rows.add(BigDecimal.valueOf(100*i));
        }

        // when
        Tool tool = new Tool();
        log.info("###############{}", tool.graph("Test Graph", rows));
    }

}
