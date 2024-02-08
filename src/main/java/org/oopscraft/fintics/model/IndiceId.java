package org.oopscraft.fintics.model;

public enum IndiceId {

    NDX("Nasdaq"),
    NDX_FUTURE("Nasdaq Future"),
    SPX("S&P 500"),
    SPX_FUTURE("S&P 500 Future"),
    KOSPI("KOSPI"),
    USD_KRW("USD/KRW"),
    BITCOIN("Bitcoin");

    private final String indiceName;

    IndiceId(String indiceName) {
        this.indiceName = indiceName;
    }

    public String getIndiceName() {
        return indiceName;
    }

}
