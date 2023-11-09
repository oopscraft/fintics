import java.time.LocalTime;

def getIndicatorVotes(indicator, period) {
    def votes = [];

    // EMA
    def emas = indicator.getMinuteEmas(60);
    votes.add(tool.slope(emas, period) > 0 ? 100 : 0);

    // MACD
    def macds = indicator.getMinuteMacds(60, 120, 40);
    def macdOscillators = macds.collect{it.oscillator};
    votes.add(tool.slope(macdOscillators, period) > 0 ? 100 : 0);
    votes.add(tool.average(macdOscillators, period) > 0 ? 100 : 0);

    // RSI
    def rsis = indicator.getMinuteRsis(60);
    votes.add(tool.slope(rsis, period) > 0 ? 100 : 0);
    votes.add(tool.average(rsis, period) > 50 ? 100 : 0);

    // DMI
    def dmis = indicator.getMinuteDmis(60);
    def dmiPdis = dmis.collect{it.pdi};
    def dmiMdis = dmis.collect{it.mdi};
    votes.add(tool.average(dmiPdis, period) > tool.average(dmiMdis, period) ? 100 : 0);
    votes.add(tool.slope(dmiPdis, period) > 0 ? 100 : 0);
    votes.add(tool.slope(dmiMdis, period) < 0 ? 100 : 0);

    // return
    return votes;
}

Boolean hold;
int period = 10;
log.info("assetIndicator.minuteOhlcv:{}", assetIndicator.getMinuteOhlcv());
def priceEmaSlope = tool.slope(assetIndicator.getMinuteEmas(60), period);

// 매수조건
if(priceEmaSlope > 0) {
    def buyVotes = [];
    buyVotes.add(getIndicatorVotes(assetIndicator, period).average() > 50 ? 100 : 0);

    // 코스피체크(하락시 매수)
    buyVotes.add(getIndicatorVotes(market.getKospiIndicator(), period).average() < 50 ? 100 : 0);

    // 달러환율체크(상승시 매수)
    buyVotes.add(getIndicatorVotes(market.getUsdKrwIndicator(), period).average() > 50 ? 100 : 0);

    // 나스닥선물체크(하락시 매수)
    buyVotes.add(getIndicatorVotes(market.getNdxFutureIndicator(), period).average() < 50 ? 100 : 0);

    // 전일나스닥지수체크(하락시 매수)
    buyVotes.add(getIndicatorVotes(market.getNdxFutureIndicator(), period).average() < 50 ? 100 : 0);

    log.info("buyVotes[{}] - {}", buyVotes.average(), buyVotes);
    if(buyVotes.average() > 60) {
        hold = true;
    }
}

// 매도조건
if(priceEmaSlope < 0) {
    def sellVotes = [];
    sellVotes.add(getIndicatorVotes(assetIndicator, period).average() < 50 ? 100 : 0);

    // 코스피체크(상승시 매도)
    sellVotes.add(getIndicatorVotes(market.getKospiIndicator(), period).average() > 50 ? 100 : 0);

    // 달러환율체크(하락시 매도)
    sellVotes.add(getIndicatorVotes(market.getUsdKrwIndicator(), period).average() < 50 ? 100 : 0);

    // 나스닥선물체크(상승시 매도)
    sellVotes.add(getIndicatorVotes(market.getNdxFutureIndicator(), period).average() > 50 ? 100 : 0);

    // 전일나스닥지수체크(상승시 매도)
    sellVotes.add(getIndicatorVotes(market.getNdxFutureIndicator(), period).average() > 50 ? 100 : 0);

    log.info("sellVotes[{}] - {}", sellVotes.average(), sellVotes);
    if(sellVotes.average() > 40) {
        hold = true;
    }
}

// 결과반환
return hold;
