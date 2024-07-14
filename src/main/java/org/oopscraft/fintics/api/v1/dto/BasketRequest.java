package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Basket;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasketRequest {

    private String basketId;

    private String basketName;

    private String market;

    private boolean rebalanceEnabled;

    private String rebalanceSchedule;

    private Basket.Language language;

    private String script;

    @Builder.Default
    private List<BasketAssetRequest> basketAssets = new ArrayList<>();

}
