package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class EmaCalculatorTest {

    @Test
    void calculate() {
        List<Double> prices = new ArrayList<>();
        prices.add(100.0);
        prices.add(105.0);
        prices.add(110.0);
        prices.add(115.0);
        prices.add(120.0);
        prices.add(125.0);

        int period = 5; // EMA 주기

        List<BigDecimal> emaValues = EmaCalculator.calculate(prices.stream()
                .map(BigDecimal::valueOf)
                .collect(Collectors.toList()), period);

        // 예상되는 EMA 값들을 정의
        double[] expectedEmaValues = {100.0, 102.5, 106.25, 110.125, 114.0625, 119.03125};

//        // 예상된 값과 실제 값이 같은지 확인
//        for (int i = 0; i < expectedEmaValues.length; i++) {
//            assertEquals(expectedEmaValues[i], emaValues.get(i), 0.001); // 정확도를 위해 허용 오차 0.001 사용
//        }
    }


}