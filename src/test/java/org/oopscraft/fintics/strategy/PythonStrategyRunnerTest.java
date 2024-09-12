package org.oopscraft.fintics.strategy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.Strategy;
import org.oopscraft.fintics.strategy.PythonStrategyRunner;

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