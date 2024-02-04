package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.IndiceId;
import org.oopscraft.fintics.model.IndiceOhlcv;
import org.oopscraft.fintics.model.Ohlcv;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class IndiceOhlcvResponse extends OhlcvResponse {

    private IndiceId indiceId;

    public static IndiceOhlcvResponse from(IndiceOhlcv indiceOhlcv) {
        return IndiceOhlcvResponse.builder()
                .indiceId(indiceOhlcv.getIndiceId())
                .type(indiceOhlcv.getType())
                .dateTime(indiceOhlcv.getDateTime())
                .openPrice(indiceOhlcv.getOpenPrice())
                .highPrice(indiceOhlcv.getOpenPrice())
                .lowPrice(indiceOhlcv.getLowPrice())
                .closePrice(indiceOhlcv.getClosePrice())
                .volume(indiceOhlcv.getVolume())
                .build();
    }

}