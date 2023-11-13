package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Indice {

    private IndiceSymbol symbol;

    private String name;

    public static Indice from(IndiceSymbol indiceSymbol) {
        return Indice.builder()
                .symbol(indiceSymbol)
                .name(indiceSymbol.getValue())
                .build();
    }

}
