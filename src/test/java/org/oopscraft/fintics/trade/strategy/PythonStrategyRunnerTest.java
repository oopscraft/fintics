package org.oopscraft.fintics.trade.strategy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.Strategy;

@Slf4j
class PythonStrategyRunnerTest {

    String getScript() {
        return "print('test')\n";
    }

    @Test
    void execute() {
        // given
        Strategy strategy = Strategy.builder()
                .script(getScript())
                .build();
        // when
        PythonStrategyRunner pythonStrategyRunner = new PythonStrategyRunner(
                strategy,
                null,
                null,
                null,
                null,
                null,
                null
        );
        pythonStrategyRunner.run();
        // then
    }

}