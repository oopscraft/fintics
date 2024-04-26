package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.Ohlcv;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InterpolateIndiceOhlcvRequest {

    private Indice.Id indiceId;

    private Ohlcv.Type type;

    private ZonedDateTime dateTimeFrom;

    private ZonedDateTime dateTimeTo;

}
