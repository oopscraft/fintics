package org.oopscraft.fintics.thread;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

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
        Tool tool = new Tool();
        BigDecimal zScore = tool.zScore(values, 10);

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
        Tool tool = new Tool();
        BigDecimal zScore = tool.zScore(values, 10);

        // then
        log.info("zScore:{}", zScore.doubleValue());
        assertTrue(zScore.doubleValue() > 1);
    }

    @Test
    void slope() {
        // given
        List<BigDecimal> values = Stream.of(30,20,100,200)
                .map(BigDecimal::valueOf)
                .toList();

        // when
        Tool tool = new Tool();
        BigDecimal slope = tool.slope(values, 2);

        // then
        assertTrue(slope.doubleValue() > 0);
    }

    @Test
    @Order(1)
    void averageDefault() {
        // given
        List<BigDecimal> values = Stream.of(10,20,30,40,50)
                .map(BigDecimal::valueOf)
                .toList();

        // when
        Tool tool = new Tool();
        BigDecimal average = tool.average(values, 2);

        // then
        assertEquals(15, average.doubleValue(), 0.01);
    }

    @Test
    @Order(2)
    void averageEmptyValues() {
        // given
        List<BigDecimal> values = new ArrayList<>();

        // when
        Tool tool = new Tool();
        BigDecimal average = tool.average(values, 2);

        // then
        assertEquals(0, average.doubleValue(), 0.1);
    }

    @Test
    @Order(3)
    void averagePeriodOverSize() {
        // given
        List<BigDecimal> values = Stream.of(10,20)
                .map(BigDecimal::valueOf)
                .toList();

        // when
        Tool tool = new Tool();
        BigDecimal average = tool.average(values, 10);

        // then
        assertEquals(15, average.doubleValue(), 0.01);
    }


}
