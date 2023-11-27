package org.oopscraft.fintics.model;

import lombok.*;
import org.oopscraft.fintics.dao.TradeEntity;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Trade {

    private String tradeId;

    private String name;

    private boolean enabled;

    private Integer interval;

    private LocalTime startAt;

    private LocalTime endAt;

    private String clientType;

    private String clientProperties;

    private String holdCondition;

    private String alarmId;

    private boolean alarmOnError;

    private boolean alarmOnOrder;

    private String userId;

    @Builder.Default
    private List<TradeAsset> tradeAssets = new ArrayList<>();

    public static Trade from(TradeEntity tradeEntity) {
        Trade trade = Trade.builder()
                .tradeId(tradeEntity.getTradeId())
                .name(tradeEntity.getName())
                .enabled(tradeEntity.isEnabled())
                .interval(tradeEntity.getInterval())
                .startAt(tradeEntity.getStartAt())
                .endAt(tradeEntity.getEndAt())
                .clientType(tradeEntity.getClientType())
                .clientProperties(tradeEntity.getClientProperties())
                .holdCondition(tradeEntity.getHoldCondition())
                .alarmId(tradeEntity.getAlarmId())
                .alarmOnError(tradeEntity.isAlarmOnError())
                .alarmOnOrder(tradeEntity.isAlarmOnOrder())
                .userId(tradeEntity.getUserId())
                .build();

        // trade assets
        List<TradeAsset> tradeAssets = tradeEntity.getTradeAssetEntities().stream()
                .map(TradeAsset::from)
                .collect(Collectors.toList());
        trade.setTradeAssets(tradeAssets);

        // return
        return trade;
    }

}
