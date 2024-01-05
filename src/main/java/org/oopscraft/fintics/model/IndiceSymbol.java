package org.oopscraft.fintics.model;

public enum IndiceSymbol {

    NDX("Nasdaq"),
    NDX_FUTURE("Nasdaq Future"),
    SPX("S&P 500"),
    SPX_FUTURE("S&P 500 Future"),
    KOSPI("KOSPI"),
    USD_KRW("USD/KRW"),
    BITCOIN("Bitcoin");

    private final String value;

    IndiceSymbol(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
