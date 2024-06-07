package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Order;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeRequest {

    private String tradeId;

    private String tradeName;

    private boolean enabled;

    private Integer interval;

    private Integer threshold;

    private LocalTime startAt;

    private LocalTime endAt;

    private BigDecimal investAmount;

    private String brokerId;

    private String strategyId;

    private String strategyVariables;

    private Order.Kind orderKind;

    private String alarmId;

    private boolean alarmOnError;

    private boolean alarmOnOrder;

    @Builder.Default
    private List<TradeAssetResponse> tradeAssets = new ArrayList<>();

}
