import java.time.LocalTime;

Boolean hold;

// info
def name = assetIndicator.getName();

// OHLCV
def ohlcvs = tool.resample(assetIndicator.getMinuteOhlcvs(), 1);
def ohlcv = ohlcvs.first();
log.info("[{}] ohlcv:{}", assetIndicator.getName(), ohlcv);

// price
def prices = ohlcvs.collect{it.closePrice};
def price = prices.first();
def pricePctChange = tool.pctChange(prices).first();

// shortMa
def shortMas = tool.ema(ohlcvs, 20);
def shortMa = shortMas.first();
def shortMaPctChange = tool.pctChange(shortMas).first();

// longMa
def longMas = tool.ema(ohlcvs, 60);
def longMa = longMas.first();
def longMaPctChange = tool.pctChange(longMas).first();

// logging
log.info("== [{}] price:{}({}%), shortMa:{}({}%), longMa:{}({}%)",
        name, price, pricePctChange, shortMa, shortMaPctChange, longMa, longMaPctChange);

// z-score
def pricePctChangeZScore = tool.zScore(tool.pctChange(prices).take(10)).first();
log.info("== [{}] pricePctChangeZScore:{}", name, pricePctChangeZScore);

// buy
if(pricePctChangeZScore > 1
&& (price > shortMa && shortMa > longMa)
&& (pricePctChange > 0 && shortMaPctChange > 0 && longMaPctChange > 0)
){
    hold = true;
}

// sell
if(pricePctChangeZScore < -1
&& (price < shortMa)
&& (pricePctChange < shortMaPctChange)
){
    hold = false;
}

// return
log.info("== [{}] hold:{}", name, hold);
return hold;

