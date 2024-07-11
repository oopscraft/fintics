package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrderSearch {

    private String tradeId;

    private String assetId;

    private String assetName;

    private Order.Type type;

    private Order.Result result;

}
