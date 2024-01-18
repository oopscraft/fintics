package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class IndiceIndicator extends Indicator {

    private IndiceId indiceId;

    String getIndiceName() {
        return indiceId.getValue();
    }

    @Override
    public String getIndicatorName() {
        return getIndiceName();
    }

}
