package org.oopscraft.fintics.trade;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Message {

    private final String tradeId;

    private final String assetId;

    private final String body;

}
