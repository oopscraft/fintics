package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Indice;
import org.oopscraft.fintics.model.IndiceId;

@Builder
@Getter
public class IndiceResponse {

    private IndiceId id;

    private String name;

    public static IndiceResponse from(Indice indice) {
        return IndiceResponse.builder()
                .id(indice.getId())
                .name(indice.getName())
                .build();
    }

}
