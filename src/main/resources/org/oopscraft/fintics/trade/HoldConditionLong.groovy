import java.time.LocalTime;

Boolean hold;
int period = 3;

// price and volume
log.info("ohlcv: {}", assetIndicator.getMinuteOhlcv());
def prices = assetIndicator.getMinutePrices();
def priceZScore = tool.zScore(prices, 10);
def priceAverage = tool.average(prices, period);
def priceSlope = tool.slope(prices, period);
def volumes = assetIndicator.getMinuteVolumes();
def volumeZScore = tool.zScore(volumes, 10);
def volumeAverage = tool.average(volumes, period);
def volumeSlope = tool.slope(volumes, period);

// EMA
def emas = assetIndicator.getMinuteEmas(10);
def emaAverage = tool.average(emas, period);
def emaSlope = tool.slope(emas, period);

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

// RSI
def rsis = assetIndicator.getMinuteRsis(14);
def rsiAverage = tool.average(rsis, period);
def rsiSlope = tool.slope(rsis, period);

// DMI
def dmis = assetIndicator.getMinuteDmis(14);
def dmiPdis = dmis.collect{it.pdi};
def dmiPdiAverage = tool.average(dmiPdis, period);
def dmiPdiSlope = tool.slope(dmiPdis, period);
def dmiMdis = dmis.collect{it.mdi}
def dmiMdiAverage = tool.average(dmiMdis, period);
def dmiMdiSlope = tool.slope(dmiMdis, period);
def dmiAdxs = dmis.collect{it.adx};
def dmiAdxAverage = tool.average(dmiAdxs, period);
def dmiAdxSlope = tool.average(dmiAdxs, period);

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


def printInfo = {
    log.info("priceZScore: {}", priceZScore);
    log.info("priceAverage: {}", priceAverage);
    log.info("priceSlope: {}", priceSlope);
    log.info("volumeZScore: {}", volumeZScore);
    log.info("volumeAverage: {}", volumeAverage);
    log.info("volumeSlope: {}", volumeSlope);
    log.info("shortEmaAverage: {}", emaAverage);
    log.info("shortEmaSlope: {}", emaSlope);
    log.info("maceValueAverage: {}", macdValueAverage);
    log.info("macdValueSlope: {}", macdValueSlope);
    log.info("macdSignalAverage: {}", macdSignalAverage);
    log.info("macdSignalSlope: {}", macdSignalSlope);
    log.info("macdOscillatorAverage: {}", macdOscillatorAverage);
    log.info("macdOscillatorSlope: {}", macdOscillatorSlope);
    log.info("rsiAverage: {}", rsiAverage);
    log.info("rsiSlope: {}", rsiSlope);
    log.info("dmiPdiAverage: {}", dmiPdiAverage);
    log.info("dmiPdiSlope: {}", dmiPdiSlope);
    log.info("dmiMdiAverage: {}", dmiMdiAverage);
    log.info("dmiMdiSlope: {}", dmiMdiSlope);
    log.info("dmiAdxAverage: {}", dmiAdxAverage);
    log.info("dmiAdxSlope: {}", dmiAdxSlope);
}

// logging
log.info("[{}] priceZScore:{}, volumeZScore:{}", assetIndicator.getName(), priceZScore, volumeZScore);

// 매수조건
if((priceZScore > 1.0 && volumeZScore > 1.0)
&& (priceSlope > 0 && priceAverage > emaAverage)
) {
    def buyVotes = [];

    // 대상종목 보조지표 매수 조건
    buyVotes.add(macdValueSlope > 0 ? 100 : 0);
    buyVotes.add(macdValueAverage > 0 ? 100 : 0);
    buyVotes.add(macdValueAverage > macdSignalAverage ? 100 : 0);
    buyVotes.add(macdOscillatorAverage > 0 ? 100 : 0);
    buyVotes.add(rsiSlope > 0 ? 100 : 0);
    buyVotes.add(rsiAverage > 50 ? 100 : 0);

    /*
    // 코스피 하락시 매수(인버스)
    buyVotes.add(kospiEmaSlope < 0 ? 100 : 0);

    // 달러환율 하락시 매수
    buyVotes.add(usdKrwEmaSlope < 0 ? 100 : 0);

    // 나스닥선물 상승시 매수
    buyVotes.add(ndxFutureEmaSlope > 0 ? 100 : 0);
    */

    // 매수여부 결과
    log.info("buyVotes[{}] - {}/{}", assetIndicator.getName(), buyVotes.average(), buyVotes);
    if(buyVotes.average() > 50) {
        hold = true;
    }
}

// 매도조건
if((priceZScore < -1.0 && volumeZScore > 1.0)
&& (priceSlope < 0 && priceAverage < emaAverage)
) {
    def sellVotes = [];

    // 대상종목 보조지표 매도 조건
    sellVotes.add(macdValueSlope < 0 ? 100 : 0);
    sellVotes.add(macdValueAverage < 0 ? 100 : 0);
    sellVotes.add(macdValueAverage < macdSignalAverage ? 100 : 0);
    sellVotes.add(macdOscillatorAverage < 0 ? 100 : 0);
    sellVotes.add(rsiSlope < 0 ? 100 : 0);
    sellVotes.add(rsiAverage < 50 ? 100 : 0);

    /*
    // 코스피 하락시 매도
    sellVotes.add(kospiEmaSlope < 0 ? 100 : 0);

    // 달러환율 상승시 매도
    sellVotes.add(usdKrwEmaSlope > 0 ? 100 : 0);

    // 나스닥선물 하락시 매도
    sellVotes.add(ndxFutureEmaSlope < 0 ? 100 : 0);
     */

    // 매도여부 결과
    log.info("sellVotes[{}] - {}/{}", assetIndicator.getName(), sellVotes.average(), sellVotes);
    if(sellVotes.average() > 50) {
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
