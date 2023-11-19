import java.time.LocalTime;

Boolean hold;

// OHLCV
def ohlcvs = tool.resample(assetIndicator.getMinuteOhlcvs(), 5, 'ohlcv');
def ohlcv = ohlcvs.get(0);
log.info("[{}] ohlcv:{}", assetIndicator.getName(), ohlcv);

// price, volume
def prices = ohlcvs.collect{it.closePrice};
def price = prices[0];
def volumes = ohlcvs.collect{it.volume};
def volume = volumes[0];

// EMA
def emas = tool.ema(ohlcvs, 12);
def ema = emas.get(0);

// MACD
def macds = tool.macd(ohlcvs, 12, 16, 9);
def macdValue = macds[0].value;
def macdSignal = macds[0].signal;
def macdValueSlope = tool.slope(macds.collect{it.value}[0..2]);
def macdSignalSlope = tool.slope(macds.collect{it.signal}[0..2]);

// RSI
def rsis = tool.rsi(ohlcvs, 14);
def rsi = rsis[0];
def rsiSlope = tool.slope(rsis[0..2]);

// DMI
def dmis = tool.dmi(ohlcvs, 14);
def dmiPdi = dmis[0].pdi;
def dmiMdi = dmis[0].mdi;
def dmiAdx = dmis[0].adx;

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

// z-score
def priceZScore = tool.zScore(prices, price);
def volumeZScore = tool.zScore(volumes, volume);
log.info("[{}] priceZScore:{}, volumeZScore:{}", assetIndicator.getName(), priceZScore, volumeZScore);

def printTaInfo = {
    log.info("[{}] Technical Analysis", assetIndicator.getName());
    log.info("price:{}", price);
    log.info("volume:{}", volume);
    log.info("ema:{}", ema);
    log.info("macdValue:{}", macdValue);
    log.info("macdSignal:{}", macdSignal);
    log.info("macdValueSlope:{}", macdValueSlope);
    log.info("macdSignalSlope:{}", macdSignalSlope);
    log.info("rsi:{}", rsi);
    log.info("rsiSlope:{}", rsiSlope);
    log.info("dmiPdi:{}", dmiPdi);
    log.info("dmiMdi:{}", dmiMdi);
    log.info("dmiAdx:{}", dmiAdx);
}

printTaInfo();

// buy condition
if(priceZScore > 1 && volumeZScore > 1) {
    log.info("######### buy votes ##########");
    def buyVotes = [];

    // technical analysis
    buyVotes.add(price > ema ? 100 : 0);
    buyVotes.add(macdValue > 0 ? 100 : 0);
    buyVotes.add(macdValue > macdSignal ? 100 : 0);
    buyVotes.add(rsiSlope > 0 ? 100 : 0);
    buyVotes.add(rsi > 50 ? 100 : 0);

    /*
    // 코스피 하락시 매수(인버스)
    buyVotes.add(kospiEmaSlope < 0 ? 100 : 0);

    // 달러환율 하락시 매수
    buyVotes.add(usdKrwEmaSlope < 0 ? 100 : 0);

    // 나스닥선물 상승시 매수
    buyVotes.add(ndxFutureEmaSlope > 0 ? 100 : 0);
    */

    // buy result
    log.info("buyVotes[{}] - {}/{}", assetIndicator.getName(), buyVotes.average(), buyVotes);
    if(buyVotes.average() > 50) {
        hold = true;
    }
}

// sell condition
if(priceZScore < -1 && volumeZScore > 1) {
    log.info("######### sell votes ##########");
    def sellVotes = [];

    // technical analysis
    sellVotes.add(price < ema ? 100 : 0);
    sellVotes.add(macdValue < 0 ? 100 : 0);
    sellVotes.add(macdValue < macdSignal ? 100 : 0);
    sellVotes.add(rsiSlope < 0 ? 100 : 0);
    sellVotes.add(rsi < 50 ? 100 : 0);

    /*
    // 코스피 하락시 매도
    sellVotes.add(kospiEmaSlope < 0 ? 100 : 0);

    // 달러환율 상승시 매도
    sellVotes.add(usdKrwEmaSlope > 0 ? 100 : 0);

    // 나스닥선물 하락시 매도
    sellVotes.add(ndxFutureEmaSlope < 0 ? 100 : 0);
    */

    // buy result
    log.info("sellVotes[{}] - {}/{}", assetIndicator.getName(), sellVotes.average(), sellVotes);
    if(sellVotes.average() > 50) {
        hold = true;
    }
}

// return
return hold;
