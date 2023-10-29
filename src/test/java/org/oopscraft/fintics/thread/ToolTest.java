package org.oopscraft.fintics.thread;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ToolTest {

    @Test
    @Order(1)
    void averageDefault() {
        // given
        List<BigDecimal> values = new ArrayList<>(){{
            add(BigDecimal.valueOf(10.0));
            add(BigDecimal.valueOf(20.0));
        }};

        // when
        Tool tool = new Tool();
        BigDecimal average = tool.average(values, 2);

        // then
        assertEquals(15, average.doubleValue(), 0.1);
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
        List<BigDecimal> values = new ArrayList<>(){{
            add(BigDecimal.valueOf(10.0));
            add(BigDecimal.valueOf(20.0));
        }};

        // when
        Tool tool = new Tool();
        BigDecimal average = tool.average(values, 10);

        // then
        assertEquals(15, average.doubleValue(), 0.1);
    }

    @Test
    @Order(4)
    void averagePeriodUnderSize() {
        // given
        List<BigDecimal> values = new ArrayList<>() {{
                add(BigDecimal.valueOf(1.0));
                add(BigDecimal.valueOf(2.0));
                add(BigDecimal.valueOf(3.0));
                add(BigDecimal.valueOf(4.0));
        }};

        // when
        Tool tool = new Tool();
        BigDecimal average = tool.average(values, 2);

        // then
        assertEquals(1.5, average.doubleValue(), 0.1);
    }

}
