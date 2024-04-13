package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Indice;

@Builder
@Getter
public class IndiceResponse {

    private Indice.Id indiceId;

    private String indiceName;

    public static IndiceResponse from(Indice indice) {
        return IndiceResponse.builder()
                .indiceId(indice.getIndiceId())
                .indiceName(indice.getIndiceName())
                .build();
    }

}
