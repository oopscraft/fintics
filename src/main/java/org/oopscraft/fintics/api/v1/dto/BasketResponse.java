package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Basket;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasketResponse {

    private String basketId;

    private String basketName;

    private String market;

    private boolean rebalanceEnabled;

    private String rebalanceSchedule;

    private Basket.Language language;

    private String script;

    @Builder.Default
    private List<BasketAssetResponse> basketAssets = new ArrayList<>();

    /**
     * from factory method
     * @param basket basket
     * @return basket response
     */
    public static BasketResponse from(Basket basket) {
        return BasketResponse.builder()
                .basketId(basket.getBasketId())
                .basketName(basket.getBasketName())
                .market(basket.getMarket())
                .rebalanceEnabled(basket.isRebalanceEnabled())
                .rebalanceSchedule(basket.getRebalanceSchedule())
                .language(basket.getLanguage())
                .script(basket.getScript())
                .basketAssets(basket.getBasketAssets().stream()
                        .map(BasketAssetResponse::from)
                        .toList())
                .build();
    }

}
