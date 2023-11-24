import java.time.LocalTime

Boolean hold;

// info
def name = assetIndicator.getName();

// OHLCV(기본 1분 데이터)
def ohlcvs = tool.resample(assetIndicator.getMinuteOhlcvs(), 1);
def ohlcv = ohlcvs.first();

// price
def prices = ohlcvs.collect{it.closePrice};
def price = prices.first();
def pricePctChange = tool.sum(tool.pctChange(prices).take(5));

// volume
def volumes = ohlcvs.collect{it.volume};
def volume = volumes.first();
def volumePctChange = tool.sum(tool.pctChange(volumes).take(5));

// shortMa
def shortMas = tool.ema(ohlcvs, 10);
def shortMa = shortMas.first();
def shortMaPctChange = tool.sum(tool.pctChange(shortMas).take(5));

// longMa
def longMas = tool.ema(ohlcvs, 30);
def longMa = longMas.first();
def longMaPctChange = tool.sum(tool.pctChange(longMas).take(5));

// macd
def macds = tool.macd(ohlcvs, 12, 26, 9);
def macd = macds.first();
def macdValuePctChange = tool.sum(tool.pctChange(macds.collect{it.value}).take(5));

// rsi
def rsis = tool.rsi(ohlcvs, 14);
def rsi = rsis.first();
def rsiPctChange = tool.sum(tool.pctChange(rsis).take(5));

// dmi
def dmis = tool.dmi(ohlcvs, 14);
def dmi = dmis.first();
def dmiPdiPctChange = tool.sum(tool.pctChange(dmis.collect{it.pdi}).take(5));
def dmiMdiPctChange = tool.sum(tool.pctChange(dmis.collect{it.mdi}).take(5));

// obv
def obvs = tool.obv(ohlcvs);
def obv = obvs.first();
def obvPctChange = tool.sum(tool.pctChange(obvs).take(5));

// kospi indice
def kospiIndicator = indiceIndicators['KOSPI'];
def kospiOhlcvs = tool.resample(kospiIndicator.getMinuteOhlcvs(), 10);
def kospiMacd = tool.macd(kospiOhlcvs, 12, 16, 9).first();

// USD/KRW
def usdKrwIndicator = indiceIndicators['USD_KRW'];
def usdKrwOhlcvs = tool.resample(usdKrwIndicator.getMinuteOhlcvs(), 10);
def usdKrwMacd = tool.macd(usdKrwOhlcvs, 12, 16, 9).first();

// Nasdaq future
def ndxFutureIndicator = indiceIndicators['NDX_FUTURE'];
def ndxFutureOhlcvs = tool.resample(ndxFutureIndicator.getMinuteOhlcvs(), 10);
def ndxFutureMacd = tool.macd(ndxFutureOhlcvs, 12, 16, 9).first();

// hold vote
def holdVote = [:];
holdVote.pricePctChange = (pricePctChange > 0.0 ? 100 : 0);
holdVote.priceShortMa = (price > shortMa ? 100 : 0);
holdVote.priceLongMa = (price > longMa ? 100 : 0);
holdVote.shortMaLongMa = (shortMa > longMa ? 100 : 0);
holdVote.shortMaPctChange = (shortMaPctChange > 0.0 ? 100 : 0);
holdVote.longMaPctChange = (longMaPctChange > 0.0 ? 100 : 0);
holdVote.macdValue = (macd.value > 0 ? 100 : 0);
holdVote.macdOscillator = (macd.oscillator > 0 ? 100 : 0);
holdVote.macdValuePctChange = (macdValuePctChange > 0 ? 100 : 0);
holdVote.rsi = (rsi > 50 ? 100 : 0);
holdVote.rsiPctChange = (rsiPctChange > 0.0 ? 100 : 0);
holdVote.dmiPdi = (dmi.pdi > dmi.mdi ? 100 : 0);
holdVote.dmiPdiPctChange = (dmiPdiPctChange > 0.0 ? 100 : 0);
holdVote.dmiMdiPctChange = (dmiMdiPctChange < 0.0 ? 100 : 0);
holdVote.dmiAdx = (dmi.adx > 20 && dmi.pdi - dmi.mdi > 10 ? 100 : 0);
holdVote.obvPctChage = (obvPctChange > 0.0 ? 100 : 0);

// hold vote - indice
holdVote.kospiMacdValue = (kospiMacd.value > 0 ? 100 : 0);          // kospi 지수 상승 시 매수
holdVote.usdKrwMacdValue = (usdKrwMacd.value < 0 ? 100 : 0);        // 달러 환율 하락 시 매수
holdVote.ndxFutureMacdValue = (ndxFutureMacd.value > 0 ? 100 : 0);  // 나스닥 선물 상승 시 매수

// hold vote result
def holdVoteResult = holdVote.values()
        .toList()
        .average();

// logging
log.info("[{}] orderBook:{}", name, orderBook);
log.info("[{}] ohlcv:{}", name, ohlcv);
log.info("[{}] price:{}({}%)", name, price, pricePctChange);
log.info("[{}] volume:{}({}%)", name, volume, volumePctChange);
log.info("[{}] shortMa:{}({}%)", name, shortMa, shortMaPctChange);
log.info("[{}] longMa:{}({}%)", name, longMa, longMaPctChange);
log.info("[{}] macd:{}", name, macd);
log.info("[{}] rsi:{}", name, rsi);
log.info("[{}] dmi:{}", name, dmi);
log.info("[{}] obv:{}({}%)", name, obv, obvPctChange);
log.info("[{}] kospiOhlcv:{}", kospiOhlcvs.first());
log.info("[{}] kospiMacd:{}", kospiMacd);
log.info("[{}] usdKrwOhlcv:{}", usdKrwOhlcvs.first());
log.info("[{}] usdKrwMacd:{}", usdKrwMacd);
log.info("[{}] ndxFutureOhlcv:{}", ndxFutureOhlcvs.first());
log.info("[{}] ndxFutureMacd:{}", ndxFutureMacd);
holdVote.each { key, value -> {
    log.info("[{}] holdVote[{}]:{}", name, key, value);
}};
log.info("[{}] holdVoteResult:{}", name, holdVoteResult);

// 매수 여부 판단
if(pricePctChange > 0.0) {
    if(holdVoteResult > 80) {
        hold = true;
    }
}

// 매도 여부 판단
if(pricePctChange < 0.0) {
    if(holdVoteResult < 20) {
        hold = false;
    }
}

// 장종료 전 처리 - 15:00 부터는 매수는 하지 않음
if(dateTime.toLocalTime().isAfter(LocalTime.of(15,0))) {
    if(hold) {
        hold = false;
    }
}

// 장종료 전 처리 - 15:15 이후는 모두 매도(보유 하지 않음)
if(dateTime.toLocalTime().isAfter(LocalTime.of(15, 15))) {
    hold = false;
}

// return
log.info("[{}] hold:{}", name, hold);
return hold;
