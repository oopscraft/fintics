Boolean hold;
int period = 10;

// EMA
def assetEmas = assetIndicator.getMinuteEmas(60);
def assetEmaSlope = tool.slope(assetEmas, period);
log.info("assetEmaSlope:{}", assetEmaSlope);

// MACD
def assetMacds = assetIndicator.getMinuteMacds(60, 120, 40);
def assetMacdOscillatorAverage = tool.average(assetMacds.collect{it.oscillator}, period);
log.info("assetMacdOscillatorAverage:{}", assetMacdOscillatorAverage);

// RSI
def assetRsis = assetIndicator.getMinuteRsis(60);
def assetRsiAverage = tool.average(assetRsis, period);
log.info("assetRsiAverage:{}", assetRsiAverage);

// DMI
def assetDmis = assetIndicator.getMinuteDmis(60);
def assetDmiPdiAverage = tool.average(assetDmis.collect{it.pdi}, period);
def assetDmiMdiAverage = tool.average(assetDmis.collect{it.mdi}, period);
log.info("assetDmiPdiAverage:{}", assetDmiPdiAverage);
log.info("assetDmiMdiAverage:{}", assetDmiMdiAverage);

// Kospi
def kospiEmas = market.getKospiIndicator().getMinuteEmas(60);
def kospiEmaSlope = tool.slope(kospiEmas, period);
log.info("kospiEmaSlope:{}", kospiEmaSlope);

// USD/KRW
def usdKrwEmas = market.getUsdKrwIndicator().getMinuteEmas(60);
def usdKrwEmaSlope = tool.slope(usdKrwEmas, period);
log.info("usdKrwEmaSlope:{}", usdKrwEmaSlope);

// Nasdaq Future
def ndxFutureEmas = market.getNdxFutureIndicator().getMinuteEmas(60);
def ndxFutureEmaSlope = tool.slope(ndxFutureEmas, period);
log.info("ndxFutureEmaSlope:{}", ndxFutureEmaSlope);

// 매수조건(인버스)
if(assetEmaSlope > 0) {
    def buyVotes = [];

    // 대상종목 상승시 매수
    buyVotes.add(assetEmaSlope > 0 ? 100 : 0);
    buyVotes.add(assetMacdOscillatorAverage > 0 ? 100 : 0);
    buyVotes.add(assetRsiAverage > 50 ? 100 : 0);
    buyVotes.add(assetDmiPdiAverage > assetDmiMdiAverage ? 100 : 0);

    // 코스피 하락시 매수(인버스)
    buyVotes.add(kospiEmaSlope < 0 ? 100 : 0);

    // 달러환율 상승시 매수(인버스)
    buyVotes.add(usdKrwEmaSlope > 0 ? 100 : 0);

    // 나스닥선물 하락시 매수(인버스)
    buyVotes.add(ndxFutureEmaSlope < 0 ? 100 : 0);

    // 매수여부 결과
    log.info("buyVotes[{}] - {}", buyVotes.average(), buyVotes);
    if(buyVotes.average() > 70) {
        hold = true;
    }
}

// 매도조건(인버스)
if(assetEmaSlope < 0) {
    def sellVotes = [];

    // 대상종목 하락시 매도
    sellVotes.add(assetEmaSlope < 0 ? 100 : 0);
    sellVotes.add(assetMacdOscillatorAverage < 0 ? 100 : 0);
    sellVotes.add(assetRsiAverage < 50 ? 100 : 0);
    sellVotes.add(assetDmiPdiAverage < assetDmiMdiAverage ? 100 : 0);

    // 코스피 상승시 매도(인버스)
    sellVotes.add(kospiEmaSlope > 0 ? 100 : 0);

    // 달러환율 하락시 매도(인버스)
    sellVotes.add(usdKrwEmaSlope < 0 ? 100 : 0);

    // 나스닥선물 상승시 매도(인버스)
    sellVotes.add(ndxFutureEmaSlope > 0 ? 100 : 0);

    // 매도여부 결과
    log.info("sellVotes[{}] - {}", sellVotes.average(), sellVotes);
    if(sellVotes.average() > 30) {
        hold = false;
    }
}

// 결과반환
return hold;
