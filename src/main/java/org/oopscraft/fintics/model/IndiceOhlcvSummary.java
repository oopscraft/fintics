package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IndiceOhlcvSummary extends OhlcvSummary implements Serializable {

    private Indice.Id indiceId;

    public String getIndiceName() {
        return indiceId.getIndiceName();
    }

}
