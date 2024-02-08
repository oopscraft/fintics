package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Indice {

    private IndiceId indiceId;

    private String indiceName;

    public static Indice from(IndiceId indiceSymbol) {
        return Indice.builder()
                .indiceId(indiceSymbol)
                .indiceName(indiceSymbol.getIndiceName())
                .build();
    }

}
