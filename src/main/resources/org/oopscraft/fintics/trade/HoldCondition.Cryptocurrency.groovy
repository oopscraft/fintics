Boolean hold;

// info
def name = assetIndicator.getName();

// OHLCV
def ohlcvs = tool.resample(assetIndicator.getMinuteOhlcvs(), 1);
def ohlcv = ohlcvs.first();

// price
def prices = ohlcvs.collect{it.closePrice};
def price = prices.first();
def pricePctChange = tool.sum(tool.pctChange(prices).take(5));

// shortMa
def shortMas = tool.ema(ohlcvs, 20);
def shortMa = shortMas.first();
def shortMaPctChange = tool.sum(tool.pctChange(shortMas).take(5));

// longMa
def longMas = tool.ema(ohlcvs, 60);
def longMa = longMas.first();
def longMaPctChange = tool.sum(tool.pctChange(longMas).take(5));

// macd
def macds = tool.macd(ohlcvs, 12, 26, 9);
def macd = macds.first();

// rsi
def rsis = tool.rsi(ohlcvs, 14);
def rsi = rsis.first();

// dmi
def dmis = tool.dmi(ohlcvs, 14);
def dmi = dmis.first();

// obv
def obvs = tool.obv(ohlcvs);
def obv = obvs.first();
def obvPctChange = tool.sum(tool.pctChange(obvs).take(5));

// hold votes
def holdVotes = [];
holdVotes.add(pricePctChange > 0.0 ? 100 : 0);
holdVotes.add(price > shortMa ? 100 : 0);
holdVotes.add(price > longMa ? 100 : 0);
holdVotes.add(shortMa > longMa ? 100 : 0);
holdVotes.add(shortMaPctChange > 0.0 ? 100 : 0);
holdVotes.add(longMaPctChange > 0.0 ? 100 : 0);
holdVotes.add(macd.value > 0 ? 100 : 0);
holdVotes.add(macd.oscillator > 0 ? 100 : 0);
holdVotes.add(rsi > 50 ? 100 : 0);
holdVotes.add(dmi.pdi > dmi.mdi ? 100 : 0);
holdVotes.add(dmi.pdi - dmi.mdi > 10 && dmi.adx > 25 ? 100 : 0);
holdVotes.add(obvPctChange > 0.0 ? 100 : 0);

// logging
log.info("== [{}] orderBook:{}", name, orderBook);
log.info("== [{}] ohlcv:{}", name, ohlcv);
log.info("== [{}] price:{}({}%)", name, price, pricePctChange);
log.info("== [{}] shortMa:{}({}%)", name, shortMa, shortMaPctChange);
log.info("== [{}] longMa:{}({}%)", name, longMa, longMaPctChange);
log.info("== [{}] macd:{}", name, macd);
log.info("== [{}] rsi:{}", name, rsi);
log.info("== [{}] dmi:{}", name, dmi);
log.info("== [{}] obv:{}({}%)", name, obv, obvPctChange);
log.info("== [{}] holdVotes[{}]:{}", name, holdVotes.average(), holdVotes);

// TODO 이상 거래 탐지

// 매수 여부 판단
if(pricePctChange > 0.0) {
    if(holdVotes.average() > 70) {
        hold = true;
    }
}

// 매도 여부 판단
if(pricePctChange < 0.0) {
    if(holdVotes.average() < 30) {
        hold = false;
    }
}

// return
log.info("== [{}] hold:{}", name, hold);
return hold;

