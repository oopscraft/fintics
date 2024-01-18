package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class RunSimulateRequest {

    private Trade trade;

    private ZonedDateTime dateTimeFrom;

    private ZonedDateTime dateTimeTo;

    private BigDecimal investAmount;

    @Builder.Default
    private List<IndiceIndicator> indiceIndicators = new ArrayList<>();

    @Builder.Default
    private List<AssetIndicator> assetIndicators = new ArrayList<>();

}
