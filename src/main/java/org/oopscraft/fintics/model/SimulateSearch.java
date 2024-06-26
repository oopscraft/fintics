package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SimulateSearch {

    private String tradeId;

    private Simulate.Status status;

    private Boolean favorite;

}
