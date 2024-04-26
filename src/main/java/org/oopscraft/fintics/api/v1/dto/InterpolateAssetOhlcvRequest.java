package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Ohlcv;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InterpolateAssetOhlcvRequest {

    private String assetId;

    private Ohlcv.Type type;

    private ZonedDateTime dateTimeFrom;

    private ZonedDateTime dateTimeTo;

}
