package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
class EmaCalculatorTest {

    @Test
    void calculate() {
        // given
        List<Double> prices = new ArrayList<>();
        prices.add(1000.0);
        prices.add(1005.0);
        prices.add(1010.0);
        prices.add(1015.0);
        prices.add(1020.0);
        prices.add(1025.0);
        int period = 5; // EMA 주기

        // when
        EmaCalculator emaCalculator = EmaCalculator.of(prices, period);
        List<Double> emaValues = emaCalculator.calculate();

        // then
        double[] expectedEmaValues = {100.0, 102.5, 106.25, 110.125, 114.0625, 119.03125};
        log.debug("== emaValues:{}", Arrays.toString(emaValues.toArray()));
        log.debug("== expectedEmaValues:{}", Arrays.toString(expectedEmaValues));


//        // 예상된 값과 실제 값이 같은지 확인
//        for (int i = 0; i < expectedEmaValues.length; i++) {
//            assertEquals(expectedEmaValues[i], emaValues.get(i), 0.001); // 정확도를 위해 허용 오차 0.001 사용
//        }
    }


}