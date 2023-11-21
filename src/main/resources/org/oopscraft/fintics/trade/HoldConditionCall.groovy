import java.time.LocalTime;

Boolean hold;

// info
def name = assetIndicator.getName();

// OHLCV
def ohlcvs = tool.resample(assetIndicator.getMinuteOhlcvs(), 1);
def ohlcv = ohlcvs.first();

// price
def prices = ohlcvs.collect{it.closePrice};
def price = prices.first();
def pricePctChange = tool.mean(tool.pctChange(prices).take(10));

// shortMa
def shortMas = tool.ema(ohlcvs, 20);
def shortMa = shortMas.first();
def shortMaPctChange = tool.mean(tool.pctChange(shortMas).take(10));

// longMa
def longMas = tool.ema(ohlcvs, 60);
def longMa = longMas.first();
def longMaPctChange = tool.mean(tool.pctChange(longMas).take(10));

// macd
def macds = tool.macd(ohlcvs, 12, 26, 9);
def macd = macds.first();

// rsi
def rsis = tool.rsi(ohlcvs, 14);
def rsi = rsis.first();

// dmi
def dmis = tool.dmi(ohlcvs, 14);
def dmi = dmis.first();

// logging
log.info("== [{}] ohlcv:{}", name, ohlcv);
log.info("== [{}] price:{}({}%), shortMa:{}({}%), longMa:{}({}%)", name, price, pricePctChange, shortMa, shortMaPctChange, longMa, longMaPctChange);
log.info("== [{}] macd:{}", name, macd);
log.info("== [{}] rsi:{}", name, rsi);
log.info("== [{}] dmi:{}", name, dmi);

// buy
if((price > shortMa && shortMa > longMa)
&& (pricePctChange > 0.0 && shortMaPctChange > 0.0 && longMaPctChange > 0.0)
){
    log.info("== [{}] buy vote.", name);
    hold = true;
}

// sell
if((price < longMa)
&& (pricePctChange < 0.0)
){
    log.info("== [{}] sell vote.", name);
    hold = false;
}

// return
log.info("== [{}] hold:{}", name, hold);
return hold;

