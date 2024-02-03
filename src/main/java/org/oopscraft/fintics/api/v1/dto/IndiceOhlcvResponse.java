package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.IndiceId;
import org.oopscraft.fintics.model.Ohlcv;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class IndiceOhlcvResponse extends OhlcvResponse {

    private IndiceId indiceId;

    public static IndiceOhlcvResponse from(IndiceId indiceId, Ohlcv ohlcv) {
        return IndiceOhlcvResponse.builder()
                .indiceId(indiceId)
                .type(ohlcv.getType())
                .dateTime(ohlcv.getDateTime())
                .openPrice(ohlcv.getOpenPrice())
                .highPrice(ohlcv.getOpenPrice())
                .lowPrice(ohlcv.getLowPrice())
                .closePrice(ohlcv.getClosePrice())
                .volume(ohlcv.getVolume())
                .build();
    }

}