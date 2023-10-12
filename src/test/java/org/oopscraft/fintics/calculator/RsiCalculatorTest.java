package org.oopscraft.fintics.calculator;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RsiCalculatorTest {

    @Test
    void calculate() {
        List<BigDecimal> prices = new ArrayList<>();
        // 가격 데이터 추가 (BigDecimal로 초기화)
        prices.add(new BigDecimal("45.15"));
        prices.add(new BigDecimal("45.06"));
        prices.add(new BigDecimal("45.50"));
        prices.add(new BigDecimal("45.55"));
        prices.add(new BigDecimal("45.40"));
        prices.add(new BigDecimal("45.30"));
        prices.add(new BigDecimal("45.55"));
        prices.add(new BigDecimal("45.65"));
        prices.add(new BigDecimal("45.75"));
        prices.add(new BigDecimal("45.75"));
        prices.add(new BigDecimal("45.85"));
        prices.add(new BigDecimal("46.05"));
        prices.add(new BigDecimal("46.20"));
        prices.add(new BigDecimal("46.45"));

        int period = 14; // RSI 주기

        List<BigDecimal> rsiValues = RsiCalculator.calculate(prices, period);

        // 기대값을 설정 (BigDecimal로 초기화)
        BigDecimal[] expectedRSI = {
                new BigDecimal("100.00"), // 첫 번째 값은 100으로 설정
                new BigDecimal("98.21"),
                new BigDecimal("72.73"),
                new BigDecimal("76.47"),
                new BigDecimal("60.00"),
                new BigDecimal("48.78"),
                new BigDecimal("64.71"),
                new BigDecimal("70.31"),
                new BigDecimal("75.68"),
                new BigDecimal("75.68"),
                new BigDecimal("80.56"),
                new BigDecimal("90.00"),
                new BigDecimal("93.75"),
                new BigDecimal("96.77")
        };

        // 결과 검사
        for (int i = 0; i < rsiValues.size(); i++) {
            assertEquals(expectedRSI[i], rsiValues.get(i));
        }
    }

}