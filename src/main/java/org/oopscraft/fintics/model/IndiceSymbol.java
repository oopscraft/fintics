package org.oopscraft.fintics.model;

import lombok.Getter;

public enum IndiceSymbol {

    NDX("Nasdaq"),
    NDX_FUTURE("Nasdaq Future"),
    SPX("S&P 500"),
    SPX_FUTURE("S&P 500 Future"),
    KOSPI("KOSPI"),
    USD_KRW("USD/KRW");

    private final String value;

    IndiceSymbol(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
