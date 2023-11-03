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
def spxFutureIndicator = market.getSpxFuture();
def spxFutureOhlcv = spxFutureIndicator.getDailyOhlcvs().get(0);
log.info("spxFutureOhlcv:{}", spxFutureOhlcv);
def spxFutureEmaSlope = tool.slope(spxFutureIndicator.getMinuteEmas(60), period);
def spxFutureMacds = spxFutureIndicator.getMinuteMacds(60, 120, 40);
def spxFutureMacdOscillatorSlope = tool.slope(spxFutureMacds.collect { it.oscillator }, period);
def spxFutureMacdOscillatorAverage = tool.average(spxFutureMacds.collect { it.oscillator }, period);
def spxFutureRsis = spxFutureIndicator.getMinuteRsis(60);
def spxFutureRsiSlope = tool.slope(spxFutureRsis, period);
def spxFutureRsiAverage = tool.average(spxFutureRsis, period);

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
    buyVotes.add(spxFutureEmaSlope > 0 ? marketWeight : 0);
    buyVotes.add(spxFutureMacdOscillatorAverage > 0 ? marketWeight : 0);
    buyVotes.add(spxFutureMacdOscillatorSlope > 0 ? marketWeight : 0);
    buyVotes.add(spxFutureRsiAverage > 50 ? marketWeight : 0);
    buyVotes.add(spxFutureRsiSlope > 0 ? marketWeight : 0);
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
    sellVotes.add(spxFutureEmaSlope < 0 ? marketWeight : 0);
    sellVotes.add(spxFutureMacdOscillatorAverage < 0 ? marketWeight : 0);
    sellVotes.add(spxFutureMacdOscillatorSlope < 0 ? marketWeight : 0);
    sellVotes.add(spxFutureRsiAverage < 50 ? marketWeight : 0);
    sellVotes.add(spxFutureRsiSlope < 0 ? marketWeight : 0);
    log.info("sellVotes[{}] - {}", sellVotes.average(), sellVotes);
    if(sellVotes.average() > 70) {
        hold = false;
    }
}

// 결과 반환
return hold;
