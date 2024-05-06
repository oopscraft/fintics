package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.AssetOhlcv;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.IndiceOhlcv;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IndiceOhlcvResponse extends OhlcvResponse {

    private Indice.Id indiceId;

    public static IndiceOhlcvResponse from(IndiceOhlcv indiceOhlcv) {
        return IndiceOhlcvResponse.builder()
                .indiceId(indiceOhlcv.getIndiceId())
                .dateTime(indiceOhlcv.getDateTime())
                .type(indiceOhlcv.getType())
                .openPrice(indiceOhlcv.getOpenPrice())
                .highPrice(indiceOhlcv.getHighPrice())
                .lowPrice(indiceOhlcv.getLowPrice())
                .closePrice(indiceOhlcv.getClosePrice())
                .volume(indiceOhlcv.getVolume())
                .build();
    }

}
