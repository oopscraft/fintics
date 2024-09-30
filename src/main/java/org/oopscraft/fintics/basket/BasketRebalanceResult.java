package org.oopscraft.fintics.basket;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.collections.ArrayStack;
import org.oopscraft.fintics.model.BasketAsset;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class BasketRebalanceResult {

    private Instant rebalanceDate;

    private List<BasketRebalanceAsset> basketRebalanceAssets = new ArrayList<>();

    private List<BasketAsset> addedBasketAssets = new ArrayList<>();

    private List<BasketAsset> removedBasketAssets = new ArrayList<>();

}
