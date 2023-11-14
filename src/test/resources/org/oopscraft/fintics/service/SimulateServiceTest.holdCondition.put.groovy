import java.time.LocalTime;

Boolean hold;
int period = 3;

// price
log.info("assetOhlcv: {}", assetIndicator.getMinuteOhlcv());
def assetPrices = assetIndicator.getMinutePrices();
def assetPriceAverage = tool.average(assetPrices, period);
def assetPriceSlope = tool.slope(assetPrices, period);
log.info("assetPriceAverage: {}", assetPriceAverage);
log.info("assetPriceSlop: {}", assetPriceSlope);

// EMA
def assetEmas = assetIndicator.getMinuteEmas(60);
def assetEmaAverage = tool.average(assetEmas, period);
def assetEmaSlope = tool.slope(assetEmas, period);
log.info("assetEmaAverage: {}", assetEmaAverage);
log.info("assetEmaSlope: {}", assetEmaSlope);

// MACD
def assetMacds = assetIndicator.getMinuteMacds(60, 120, 40);
def assetMacdOscillators = assetMacds.collect{it.oscillator};
def assetMacdOscillatorAverage = tool.average(assetMacdOscillators, period);
def assetMacdOscillatorSlope = tool.slope(assetMacdOscillators, period);
log.info("assetMacdOscillatorAverage: {}", assetMacdOscillatorAverage);
log.info("assetMacdOscillatorSlope: {}", assetMacdOscillatorSlope);

// RSI
def assetRsis = assetIndicator.getMinuteRsis(60);
def assetRsiAverage = tool.average(assetRsis, period);
def assetRsiSlope = tool.slope(assetRsis, period);
log.info("assetRsiAverage: {}", assetRsiAverage);
log.info("assetRsiSlope: {}", assetRsiSlope);

// DMI
def assetDmis = assetIndicator.getMinuteDmis(60);
def assetDmiPdis = assetDmis.collect{it.pdi};
def assetDmiPdiAverage = tool.average(assetDmiPdis, period);
def assetDmiPdiSlope = tool.slope(assetDmiPdis, period);
def assetDmiMdis = assetDmis.collect{it.mdi}
def assetDmiMdiAverage = tool.average(assetDmiMdis, period);
def assetDmiMdiSlop = tool.slope(assetDmiMdis, period);
def assetDmiAdxs = assetDmis.collect{it.adx};
def assetDmiAdxAverage = tool.average(assetDmiAdxs, period);
def assetDmiAdxSlope = tool.average(assetDmiAdxs, period);
log.info("assetDmiPdiAverage: {}", assetDmiPdiAverage);
log.info("assetDmiPdiSlope: {}", assetDmiPdiSlope);
log.info("assetDmiMdiAverage: {}", assetDmiMdiAverage);
log.info("assetDmiMdiSlope: {}", assetDmiMdiSlop);
log.info("assetDmiAdxAverage: {}", assetDmiAdxAverage);
log.info("assetDmiAdxSlope: {}", assetDmiAdxSlope);

// Kospi
def kospiIndicator = indiceIndicators['KOSPI'];
log.info("kospiOhlcv: {}", kospiIndicator.getMinuteOhlcv());
def kospiEmas = kospiIndicator.getMinuteEmas(60);
def kospiEmaSlope = tool.slope(kospiEmas, period);
log.info("kospiEmaSlope: {}", kospiEmaSlope);

// USD/KRW
def usdKrwIndicator = indiceIndicators['USD_KRW'];
log.info("usdKrwOhlcv: {}", usdKrwIndicator.getMinuteOhlcv());
def usdKrwEmas = usdKrwIndicator.getMinuteEmas(60);
def usdKrwEmaSlope = tool.slope(usdKrwEmas, period);
log.info("usdKrwEmaSlope: {}", usdKrwEmaSlope);

// Nasdaq Future
def ndxFutureIndicator = indiceIndicators['NDX_FUTURE'];
log.info("ndxFutureOhlcv: {}", ndxFutureIndicator.getMinuteOhlcv());
def ndxFutureEmas = ndxFutureIndicator.getMinuteEmas(60);
def ndxFutureEmaSlope = tool.slope(ndxFutureEmas, period);
log.info("ndxFutureEmaSlope: {}", ndxFutureEmaSlope);

// 매수조건(인버스)
if(assetPriceAverage > assetEmaAverage) {
    log.info("assetPriceAverage[{}] > assetEmaAverage[{}]", assetPriceAverage, assetEmaAverage);
    def buyVotes = [];

    // 대상종목 상승시 매수
    buyVotes.add(assetPriceAverage > assetEmaAverage ? 100 : 0);
    buyVotes.add(assetEmaSlope > 0 ? 100 : 0);
    buyVotes.add(assetMacdOscillatorAverage > 0 ? 100 : 0);
    buyVotes.add(assetMacdOscillatorSlope > 0 ? 100 : 0);
    buyVotes.add(assetRsiAverage > 50 ? 100 : 0);
    buyVotes.add(assetRsiSlope > 0 ? 100 : 0);
    buyVotes.add(assetRsiAverage < 30 && assetRsiSlope > 0 ? 100 : 0);
    buyVotes.add(assetDmiPdiAverage > assetDmiMdiAverage ? 100 : 0);
    buyVotes.add(assetDmiPdiSlope > assetDmiMdiSlop ? 100 : 0);
    buyVotes.add(assetDmiAdxAverage > 20 ? 100 : 0);
    buyVotes.add(assetDmiAdxSlope > 0 ? 100 : 0);

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
if(assetPriceAverage < assetEmaAverage) {
    log.info("assetPriceAverage[{}] < assetEmaAverage[{}]", assetPriceAverage, assetEmaAverage);
    def sellVotes = [];

    // 대상종목 하락시 매도
    sellVotes.add(assetPriceAverage < assetEmaAverage ? 100 : 0);
    sellVotes.add(assetEmaSlope < 0 ? 100 : 0);
    sellVotes.add(assetMacdOscillatorAverage < 0 ? 100 : 0);
    sellVotes.add(assetMacdOscillatorSlope < 0 ? 100 : 0);
    sellVotes.add(assetRsiAverage < 50 ? 100 : 0);
    sellVotes.add(assetRsiSlope < 0 ? 100 : 0);
    sellVotes.add(assetRsiAverage > 70 && assetRsiSlope < 0 ? 100 : 0);
    sellVotes.add(assetDmiPdiAverage < assetDmiMdiAverage ? 100 : 0);
    sellVotes.add(assetDmiPdiSlope < assetDmiMdiSlop ? 100 : 0);
    sellVotes.add(assetDmiAdxAverage < 20 ? 100 : 0);
    sellVotes.add(assetDmiAdxSlope < 0 ? 100 : 0);

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

// 장종료전 모두 청산(보유하지 않음)
if(dateTime.toLocalTime().isAfter(LocalTime.of(15,15))) {
    hold = false;
}

// 결과반환
return hold;
