import java.time.LocalTime;

Boolean hold;
int period = 10;
def minuteOhlcv = assetIndicator.getMinuteOhlcvs().get(0);
log.info("minuteOhlcv:{}", minuteOhlcv);

// MA indicator
def priceEma = assetIndicator.getMinuteEma(60);
def priceEmaSlope = tool.slope(assetIndicator.getMinuteEmas(60), period);
def priceSma = assetIndicator.getMinuteSma(60);
def priceSmaSlope = tool.slope(assetIndicator.getMinuteSmas(60), period);

// MACD indicator
def macds = assetIndicator.getMinuteMacds(60, 120, 40);
def macdOscillatorSlope = tool.slope(macds.collect { it.oscillator }, period);
def macdOscillatorAverage = tool.average(macds.collect { it.oscillator }, period);

// RSI indicator
def rsis = assetIndicator.getMinuteRsis(60);
def rsiSlope = tool.slope(rsis, period);
def rsiAverage = tool.average(rsis, period);

// DMI indicator
def dmis = assetIndicator.getMinuteDmis(60);
def dmiPdiAverage = tool.average(dmis.collect { it.pdi }, period);
def dmiPdiSlope = tool.slope(dmis.collect { it.pdi }, period);
def dmiMdiAverage = tool.average(dmis.collect { it.mdi }, period);
def dmiMdiSlope = tool.slope(dmis.collect { it.mdi }, period);

// daily
def dailyPeriod = 3;
def dailyOhlcv = assetIndicator.getDailyOhlcvs().get(0);
log.info("dailyOhlcv:{}", dailyOhlcv);
def dailyMacds = assetIndicator.getDailyMacds(12, 16, 9);
def dailyMacdOscillatorAverage = tool.average(dailyMacds.collect { it.oscillator }, dailyPeriod);
def dailyMacdOscillatorSlope = tool.slope(dailyMacds.collect { it.oscillator }, dailyPeriod);
def dailyRsis = assetIndicator.getDailyRsis(14);
def dailyRsiAverage = tool.average(dailyRsis, dailyPeriod);
def dailyRsiSlope = tool.slope(dailyRsis, dailyPeriod);
def dailyDmis = assetIndicator.getDailyDmis(14);
def dailyDmiPdiAverage = tool.average(dailyDmis.collect { it.pdi }, period);
def dailyDmiPdiSlope = tool.slope(dailyDmis.collect { it.pdi }, period);
def dailyDmiMdiAverage = tool.average(dailyDmis.collect { it.mdi }, period);
def dailyDmiMdiSlope = tool.slope(dailyDmis.collect { it.mdi }, period);

// market
def ndxFutureIndicator = market.getNdxFuture();
def ndxFutureOhlcv = ndxFutureIndicator.getDailyOhlcvs().get(0);
log.info("ndxFutureOhlcv:{}", ndxFutureOhlcv);
def ndxFutureEmaSlope = tool.slope(ndxFutureIndicator.getMinuteEmas(60), period);
def ndxFutureMacds = ndxFutureIndicator.getMinuteMacds(60, 120, 40);
def ndxFutureMacdOscillatorSlope = tool.slope(ndxFutureMacds.collect { it.oscillator }, period);
def ndxFutureMacdOscillatorAverage = tool.average(ndxFutureMacds.collect { it.oscillator }, period);
def ndxFutureRsis = ndxFutureIndicator.getMinuteRsis(60);
def ndxFutureRsiSlope = tool.slope(ndxFutureRsis, period);
def ndxFutureRsiAverage = tool.average(ndxFutureRsis, period);

// 매수조건
if(priceEmaSlope > 0) {
    def buyVotes = [];
    def weight = 100;
    buyVotes.add(macdOscillatorAverage > 0 ? weight : 0);
    buyVotes.add(macdOscillatorSlope > 0 ? weight : 0);
    buyVotes.add(rsiAverage > 50 ? weight : 0);
    buyVotes.add(rsiSlope > 0 ? weight : 0);
    buyVotes.add(dmiPdiAverage > dmiMdiAverage ? weight : 0);
    buyVotes.add(dmiPdiSlope > 0 ? weight : 0);
    buyVotes.add(dmiMdiSlope < 0 ? weight : 0);
    // daily factor
    def dailyWeight = 100;
    buyVotes.add(dailyMacdOscillatorAverage > 0 ? dailyWeight : 0);
    buyVotes.add(dailyMacdOscillatorSlope > 0 ? dailyWeight : 0);
    buyVotes.add(dailyRsiAverage > 50 ? dailyWeight : 0);
    buyVotes.add(dailyRsiSlope > 0 ? dailyWeight : 0);
    buyVotes.add(dailyDmiPdiAverage > dailyDmiMdiAverage ? dailyWeight : 0);
    buyVotes.add(dailyDmiPdiSlope > 0 ? dailyWeight : 0);
    buyVotes.add(dailyDmiMdiSlope < 0 ? dailyWeight : 0);
    // market factor
    def marketWeight = 100;
    buyVotes.add(ndxFutureEmaSlope > 0 ? marketWeight : 0);
    buyVotes.add(ndxFutureMacdOscillatorAverage > 0 ? marketWeight : 0);
    buyVotes.add(ndxFutureMacdOscillatorSlope > 0 ? marketWeight : 0);
    buyVotes.add(ndxFutureRsiAverage > 50 ? marketWeight : 0);
    buyVotes.add(ndxFutureRsiSlope > 0 ? marketWeight : 0);
    log.info("buyVotes[{}] - {}", buyVotes.average(), buyVotes);
    if(buyVotes.average() > 70) {
        hold = true;
    }
}

// 매도조건
if(priceEmaSlope < 0) {
    def sellVotes = [];
    def weight = 100;
    sellVotes.add(macdOscillatorAverage < 0 ? weight : 0);
    sellVotes.add(macdOscillatorSlope < 0 ? weight : 0);
    sellVotes.add(rsiAverage < 50 ? weight : 0);
    sellVotes.add(rsiSlope < 0 ? weight : 0);
    sellVotes.add(dmiPdiAverage < dmiMdiAverage ? weight : 0);
    sellVotes.add(dmiPdiSlope < 0 ? weight : 0);
    sellVotes.add(dmiMdiSlope > 0 ? weight : 0);
    // daily factor
    def dailyWeight = 100;
    sellVotes.add(dailyMacdOscillatorAverage < 0 ? dailyWeight : 0);
    sellVotes.add(dailyMacdOscillatorSlope < 0 ? dailyWeight : 0);
    sellVotes.add(dailyRsiAverage < 50 ? dailyWeight : 0);
    sellVotes.add(dailyRsiSlope < 0 ? dailyWeight : 0);
    sellVotes.add(dailyDmiPdiAverage < dailyDmiMdiAverage ? dailyWeight : 0);
    sellVotes.add(dailyDmiPdiSlope < 0 ? dailyWeight : 0);
    sellVotes.add(dailyDmiMdiSlope > 0 ? dailyWeight : 0);
    // market factor
    def marketWeight = 100;
    sellVotes.add(ndxFutureEmaSlope < 0 ? marketWeight : 0);
    sellVotes.add(ndxFutureMacdOscillatorAverage < 0 ? marketWeight : 0);
    sellVotes.add(ndxFutureMacdOscillatorSlope < 0 ? marketWeight : 0);
    sellVotes.add(ndxFutureRsiAverage < 50 ? marketWeight : 0);
    sellVotes.add(ndxFutureRsiSlope < 0 ? marketWeight : 0);
    log.info("sellVotes[{}] - {}", sellVotes.average(), sellVotes);
    if(sellVotes.average() > 70) {
        hold = false;
    }
}

// 결과 반환
return hold;
