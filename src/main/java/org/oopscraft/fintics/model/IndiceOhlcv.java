package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.IndiceOhlcvEntity;
import org.oopscraft.fintics.dao.OhlcvEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class IndiceOhlcv extends Ohlcv {

    private IndiceId indiceId;

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
