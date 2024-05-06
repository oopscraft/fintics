package org.oopscraft.fintics.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.IndiceOhlcvEntity;

@SuperBuilder
@Getter
public class IndiceOhlcv extends Ohlcv {

    private Indice.Id indiceId;

    public static IndiceOhlcv from(IndiceOhlcvEntity indiceOhlcvEntity) {
        return IndiceOhlcv.builder()
                .indiceId(indiceOhlcvEntity.getIndiceId())
                .type(indiceOhlcvEntity.getType())
                .dateTime(indiceOhlcvEntity.getDateTime())
                .openPrice(indiceOhlcvEntity.getOpenPrice())
                .highPrice(indiceOhlcvEntity.getHighPrice())
                .lowPrice(indiceOhlcvEntity.getLowPrice())
                .closePrice(indiceOhlcvEntity.getClosePrice())
                .volume(indiceOhlcvEntity.getVolume())
                .build();
    }

}
