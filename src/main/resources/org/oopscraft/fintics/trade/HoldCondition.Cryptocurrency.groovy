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
    if(holdVoteResult < 30) {
        hold = false;
    }
}

// return
log.info("[{}] hold:{}", name, hold);
return hold;
