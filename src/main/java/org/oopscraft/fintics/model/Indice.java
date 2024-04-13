package org.oopscraft.fintics.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class Indice {

    private Id indiceId;

    public String getIndiceName() {
        return this.indiceId.indiceName;
    }

    public enum Id {
        NDX("Nasdaq"),
        NDX_FUTURE("Nasdaq Future"),
        SPX("S&P 500"),
        SPX_FUTURE("S&P 500 Future"),
        KOSPI("KOSPI"),
        USD_KRW("USD/KRW"),
        BITCOIN("Bitcoin");

        @Getter
        private final String indiceName;

        Id(String indiceName) {
            this.indiceName = indiceName;
        }
    }

    public static Indice from(Indice.Id indiceId) {
        Indice indice = new Indice();
        indice.indiceId = indiceId;
        return indice;
    }

}