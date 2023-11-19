package org.oopscraft.fintics.rule;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.rule.Tools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ToolTest {

    @Test
    void zScoreNormal() {
        // given
        List<BigDecimal> values = Stream.of(10040, 10020, 10030, 9990, 10020, 10030, 10070, 10100, 10090, 10080, 10100, 10000)
                .map(BigDecimal::valueOf)
                .toList();

        // when
        Tools tool = new Tools();
        BigDecimal zScore = tool.zScore(values, BigDecimal.valueOf(10040));

        // then
        log.info("zScore:{}", zScore.doubleValue());
        assertTrue(zScore.doubleValue() <= 1);
    }

    @Test
    void zScoreOutlier() {
        // given
        List<BigDecimal> values = Stream.of(10100, 10020, 10030, 9990, 10020, 10030, 10070, 10100, 10090, 10080, 10100, 10000)
                .map(BigDecimal::valueOf)
                .toList();

        // when
        Tools tool = new Tools();
        BigDecimal zScore = tool.zScore(values, BigDecimal.valueOf(10110));

        // then
        log.info("zScore:{}", zScore.doubleValue());
        assertTrue(zScore.doubleValue() > 1);
    }

    @Test
    void slope() {
        // given
        List<BigDecimal> values = Stream.of(20,10)
                .map(BigDecimal::valueOf)
                .toList();

        // when
        Tools tool = new Tools();
        BigDecimal slope = tool.slope(values);

        // then
        log.info("== slope:{}", slope);
        assertTrue(slope.doubleValue() > 1);
    }

    @Test
    @Order(1)
    void average() {
        // given
        List<BigDecimal> values = Stream.of(10,20)
                .map(BigDecimal::valueOf)
                .toList();

        // when
        Tools tool = new Tools();
        BigDecimal average = tool.average(values);

        // then
        assertEquals(15, average.doubleValue(), 0.01);
    }

}
