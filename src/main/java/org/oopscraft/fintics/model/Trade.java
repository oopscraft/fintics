package org.oopscraft.fintics.model;

import lombok.*;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.dao.TradeEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Trade {

    private String tradeId;

    private String name;

    private boolean enabled;

    private Integer interval;

    private String clientProperties;

    private String buyRule;

    private String sellRule;

    @Builder.Default
    private List<TradeAsset> tradeAssets = new ArrayList<>();

    public static Trade from(TradeEntity tradeEntity) {
        Trade trade = Trade.builder()
                .tradeId(tradeEntity.getTradeId())
                .name(tradeEntity.getName())
                .enabled(tradeEntity.isEnabled())
                .interval(tradeEntity.getInterval())
                .clientProperties(tradeEntity.getClientProperties())
                .buyRule(tradeEntity.getBuyRule())
                .sellRule(tradeEntity.getSellRule())
                .build();
        List<TradeAsset> tradeAssets = tradeEntity.getTradeAssetEntities().stream()
                .map(tradeAssetEntity -> {
                    TradeAsset tradeAsset = TradeAsset.builder()
                            .symbol(tradeAssetEntity.getSymbol())
                            .enabled(tradeAssetEntity.isEnabled())
                            .build();
                    AssetEntity assetEntity = tradeAssetEntity.getAssetEntity();
                    if(assetEntity != null) {
                        tradeAsset.setName(assetEntity.getName());
                        tradeAsset.setType(assetEntity.getType());
                    }
                    return tradeAsset;
                })
                .collect(Collectors.toList());
        trade.setTradeAssets(tradeAssets);
        return trade;
    }

}
