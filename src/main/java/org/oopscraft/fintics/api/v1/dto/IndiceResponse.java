package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.IndiceSymbol;

@Builder
@Getter
public class IndiceResponse {

    private IndiceSymbol symbol;

    private String name;

    public static IndiceResponse from(Indice indice) {
        return IndiceResponse.builder()
                .symbol(indice.getSymbol())
                .name(indice.getName())
                .build();
    }

}
