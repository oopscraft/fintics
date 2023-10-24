package org.oopscraft.fintics.thread;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ToolTest {

    @Test
    @Order(1)
    void averageDefault() {
        // given
        List<Double> values = new ArrayList<>(){{
            add(10.0);
            add(20.0);
        }};

        // when
        Tool tool = new Tool();
        Double average = tool.average(values, 2);

        // then
        assertEquals(15, average);
    }

    @Test
    @Order(2)
    void averageEmptyValues() {
        // given
        List<Double> values = new ArrayList<>();

        // when
        Tool tool = new Tool();
        Double average = tool.average(values, 2);

        // then
        assertEquals(0, average);
    }

    @Test
    @Order(3)
    void averagePeriodUnderSize() {
        // given
        List<Double> values = new ArrayList<>(){{
            add(10.0);
            add(20.0);
        }};

        // when
        Tool tool = new Tool();
        Double average = tool.average(values, 10);

        // then
        assertEquals(15, average);
    }

}
