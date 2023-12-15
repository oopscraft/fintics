package org.oopscraft.fintics.collector;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractCollector {

    @Value("${fintics.collector.enabled:true}")
    @Getter
    private boolean enabled = true;

    public abstract void collect();

}
