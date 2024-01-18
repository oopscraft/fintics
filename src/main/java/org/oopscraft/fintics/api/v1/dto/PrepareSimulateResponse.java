package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class PrepareSimulateResponse {

    private String simulateId;

    @Builder.Default
    private List<IndiceIndicator> indiceIndicators = new ArrayList<>();

    @Builder.Default
    private List<AssetIndicator> assetIndicators = new ArrayList<>();

}
