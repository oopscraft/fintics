Boolean hold;
int period = 10;

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

// 대상종목체크
def priceEmaSlope = tool.slope(assetIndicator.getMinuteEmas(60), period);
def assetVotes = getIndicatorVotes(assetIndicator, period);
log.info("assetMinuteOhlcv:{}", assetIndicator.getMinuteOhlcv());
log.info("priceEmaSlope:{}", priceEmaSlope);
log.info("assetVotes:{}", assetVotes);

// 코스피체크
def kospiIndicator = market.getKospiIndicator();
def kospiVotes = getIndicatorVotes(kospiIndicator, period);
log.info("kospiMinuteOhlcv:{}", kospiIndicator.getMinuteOhlcv());
log.info("kospiVotes:{}", kospiVotes);

// 달러환율체크
def usdKrwIndicator = market.getUsdKrwIndicator();
def usdKrwVotes = getIndicatorVotes(usdKrwIndicator, period);
log.info("usdKrwMinuteOhlcv:{}", usdKrwIndicator.getMinuteOhlcv());
log.info("usdKrwVotes:{}", usdKrwVotes);

// 나스닥선물체크
def ndxFutureIndicator = market.getNdxFutureIndicator();
def ndxFutureVotes = getIndicatorVotes(ndxFutureIndicator, period);
log.info("ndxFutureMinuteOhlcv:{}", ndxFutureIndicator.getMinuteOhlcv());
log.info("ndxFutureVotes:{}", ndxFutureVotes);

// 전일나스닥지수체크
def ndxIndicator = market.getNdxIndicator();
def ndxVotes = getIndicatorVotes(ndxIndicator, period);
log.info("ndxMinuteOhlcv:{}", ndxIndicator.getMinuteOhlcv());
log.info("ndxVotes:{}", ndxVotes);

// 매수조건(인버스)
if(priceEmaSlope > 0) {
    def buyVotes = [];
    // 대상종목 상승시 매수
    buyVotes.add(assetVotes.average() > 50 ? 100 : 0);
    // 코스피 하락시 매수
    buyVotes.add(kospiVotes.average() < 50 ? 100 : 0);
    // 달러환율 상승시 매수
    buyVotes.add(usdKrwVotes.average() > 50 ? 100 : 0);
    // 나스닥선물 하락시 매수
    buyVotes.add(ndxFutureVotes.average() < 50 ? 100 : 0);
    // 나스닥(전일) 하락시 매수
    buyVotes.add(ndxVotes.average() < 50 ? 100 : 0);
    // 매수여부 결과
    log.info("buyVotes[{}] - {}", buyVotes.average(), buyVotes);
    if(buyVotes.average() >= 60) {
        hold = true;
    }
}

// 매도조건(인버스)
if(priceEmaSlope < 0) {
    def sellVotes = [];
    // 대상종목 하락시 매도
    sellVotes.add(assetVotes.average() < 50 ? 100 : 0);
    // 코스피 상승시 매도
    sellVotes.add(kospiVotes.average() > 50 ? 100 : 0);
    // 달러환율 하락시 매도
    sellVotes.add(usdKrwVotes.average() < 50 ? 100 : 0);
    // 나스닥선물 상승시 매도
    sellVotes.add(ndxFutureVotes.average() > 50 ? 100 : 0);
    // 나스닥(전일) 상승시 매도
    sellVotes.add(ndxVotes.average() > 50 ? 100 : 0);
    // 매도여부 결과
    log.info("sellVotes[{}] - {}", sellVotes.average(), sellVotes);
    if(sellVotes.average() >= 40) {
        hold = false;
    }
}

// 결과반환
return hold;
