package org.oopscraft.fintics.api.v1.dto;

import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeRequest {

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

    private boolean publicEnabled;

    @Builder.Default
    private List<TradeAssetResponse> tradeAssets = new ArrayList<>();

}
