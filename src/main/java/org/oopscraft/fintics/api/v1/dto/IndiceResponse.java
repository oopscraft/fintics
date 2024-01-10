package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.IndiceId;

@Builder
@Getter
public class IndiceResponse {

    private IndiceId indiceId;

    private String indiceName;

    public static IndiceResponse from(Indice indice) {
        return IndiceResponse.builder()
                .indiceId(indice.getIndiceId())
                .indiceName(indice.getIndiceName())
                .build();
    }

}
