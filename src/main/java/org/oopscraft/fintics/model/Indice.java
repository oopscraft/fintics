package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Indice {

    private IndiceId id;

    private String name;

    public static Indice from(IndiceId indiceSymbol) {
        return Indice.builder()
                .id(indiceSymbol)
                .name(indiceSymbol.getValue())
                .build();
    }

}
