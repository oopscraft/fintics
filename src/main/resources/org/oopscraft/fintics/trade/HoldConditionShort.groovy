import java.time.LocalTime;

Boolean hold;
int period = 5;

// price
log.info("ohlcv: {}", assetIndicator.getMinuteOhlcv());
def prices = assetIndicator.getMinutePrices();
def priceAverage = tool.average(prices, period);
def priceSlope = tool.slope(prices, period);
log.info("priceAverage: {}", priceAverage);
log.info("priceSlop: {}", priceSlope);

// Short EMA
def shortEmas = assetIndicator.getMinuteEmas(10);
def shortEmaAverage = tool.average(shortEmas, period);
def shortEmaSlope = tool.slope(shortEmas, period);
log.info("shortEmaAverage: {}", shortEmaAverage);
log.info("shortEmaSlope: {}", shortEmaSlope);

// Long EMA
def longEmas = assetIndicator.getMinuteEmas(60);
def longEmaAverage = tool.average(longEmas, period);
def longEmaSlope = tool.slope(longEmas, period);
log.info("longEmaAverage: {}", longEmaAverage);
log.info("longEmaSlope: {}", longEmaSlope);

// MACD
def macds = assetIndicator.getMinuteMacds(12, 26, 9);
def macdValues = macds.collect{it.value};
def macdValueAverage = tool.average(macdValues, period);
def macdValueSlope = tool.slope(macdValues, period);
def macdSignals = macds.collect{it.signal};
def macdSignalAverage = tool.average(macdSignals, period);
def macdSignalSlope = tool.slope(macdSignals, period);
def macdOscillators = macds.collect{it.oscillator};
def macdOscillatorAverage = tool.average(macdOscillators, period);
def macdOscillatorSlope = tool.slope(macdOscillators, period);
log.info("maceValueAverage: {}", macdValueAverage);
log.info("macdValueSlope: {}", macdValueSlope);
log.info("macdSignalAverage: {}", macdSignalAverage);
log.info("macdSignalSlope: {}", macdSignalSlope);
log.info("macdOscillatorAverage: {}", macdOscillatorAverage);
log.info("macdOscillatorSlope: {}", macdOscillatorSlope);

// RSI
def rsis = assetIndicator.getMinuteRsis(14);
def rsiAverage = tool.average(rsis, period);
def rsiSlope = tool.slope(rsis, period);
log.info("rsiAverage: {}", rsiAverage);
log.info("rsiSlope: {}", rsiSlope);

// DMI
def dmis = assetIndicator.getMinuteDmis(14);
def dmiPdis = dmis.collect{it.pdi};
def dmiPdiAverage = tool.average(dmiPdis, period);
def dmiPdiSlope = tool.slope(dmiPdis, period);
def dmiMdis = dmis.collect{it.mdi}
def dmiMdiAverage = tool.average(dmiMdis, period);
def dmiMdiSlop = tool.slope(dmiMdis, period);
def dmiAdxs = dmis.collect{it.adx};
def dmiAdxAverage = tool.average(dmiAdxs, period);
def dmiAdxSlope = tool.average(dmiAdxs, period);
log.info("dmiPdiAverage: {}", dmiPdiAverage);
log.info("dmiPdiSlope: {}", dmiPdiSlope);
log.info("dmiMdiAverage: {}", dmiMdiAverage);
log.info("dmiMdiSlope: {}", dmiMdiSlop);
log.info("dmiAdxAverage: {}", dmiAdxAverage);
log.info("dmiAdxSlope: {}", dmiAdxSlope);

/*
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
 */

// logging
log.info("[{}] priceAverage:{}, shortEmaAverage:{}, longEmaAverage:{}", assetIndicator.getName(), priceAverage, shortEmaAverage, longEmaAverage);

// 매수조건
if(priceSlope > 0 && priceAverage > shortEmaAverage) {
    def buyVotes = [];

    // 대상종목 보조지표 확인
    buyVotes.add(priceSlope > 0 ? 100 : 0);
    buyVotes.add(shortEmaSlope > 0 ? 100 : 0);
    buyVotes.add(longEmaSlope > 0 ? 100 : 0);
    buyVotes.add(priceAverage > shortEmaAverage ? 100 : 0);
    buyVotes.add(shortEmaAverage > longEmaAverage ? 100 : 0);
    buyVotes.add(macdValueAverage > 0 ? 100 : 0);
    buyVotes.add(macdValueAverage > macdSignalAverage ? 100 : 0);
    buyVotes.add(rsiAverage > 50 ? 100 : 0);
    buyVotes.add(rsiSlope > 0 ? 100 : 0);
    buyVotes.add(dmiPdiSlope > dmiMdiSlop ? 100 : 0);
    buyVotes.add(dmiPdiAverage > dmiMdiAverage ? 100 : 0);
    buyVotes.add(dmiAdxAverage > 20 ? 100 : 0);
    buyVotes.add(dmiAdxSlope > 0 ? 100 : 0);

    /*
    // 코스피 하락시 매수(인버스)
    buyVotes.add(kospiEmaSlope < 0 ? 100 : 0);

    // 달러환율 상승시 매수(인버스)
    buyVotes.add(usdKrwEmaSlope > 0 ? 100 : 0);

    // 나스닥선물 하락시 매수(인버스)
    buyVotes.add(ndxFutureEmaSlope < 0 ? 100 : 0);
     */

    // 매수여부 결과
    log.info("buyVotes[{}] - {}", buyVotes.average(), buyVotes);
    if(buyVotes.average() > 70) {
        hold = true;
    }
}

// 매도조건
if(priceSlope < 0 && priceAverage < shortEmaAverage) {
    def sellVotes = [];

    // 대상종목 하락시 매도
    sellVotes.add(priceSlope < 0 ? 100 : 0);
    sellVotes.add(shortEmaSlope < 0 ? 100 : 0);
    sellVotes.add(longEmaSlope < 0 ? 100 : 0);
    sellVotes.add(priceAverage < shortEmaAverage ? 100 : 0);
    sellVotes.add(shortEmaAverage < longEmaAverage ? 100 : 0);
    sellVotes.add(macdValueAverage < 0 ? 100 : 0);
    sellVotes.add(macdValueAverage < macdSignalAverage ? 100 : 0);
    sellVotes.add(rsiAverage < 50 ? 100 : 0);
    sellVotes.add(rsiSlope < 0 ? 100 : 0);
    sellVotes.add(dmiPdiSlope < dmiMdiSlop ? 100 : 0);
    sellVotes.add(dmiPdiAverage < dmiMdiAverage ? 100 : 0);
    sellVotes.add(dmiAdxAverage < 20 ? 100 : 0);
    sellVotes.add(dmiAdxSlope < 0 ? 100 : 0);

    /*
    // 코스피 상승시 매도(인버스)
    sellVotes.add(kospiEmaSlope > 0 ? 100 : 0);

    // 달러환율 하락시 매도(인버스)
    sellVotes.add(usdKrwEmaSlope < 0 ? 100 : 0);

    // 나스닥선물 상승시 매도(인버스)
    sellVotes.add(ndxFutureEmaSlope > 0 ? 100 : 0);
     */

    // 매도여부 결과
    log.info("sellVotes[{}] - {}", sellVotes.average(), sellVotes);
    if(sellVotes.average() > 30) {
        hold = false;
    }
}

/*
// 장종료전 모두 청산(보유하지 않음)
if(dateTime.toLocalTime().isAfter(LocalTime.of(15,15))) {
    hold = false;
}
 */

// 결과반환
return hold;