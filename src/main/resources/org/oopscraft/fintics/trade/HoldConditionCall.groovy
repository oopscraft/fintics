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
def pricePctChange = tool.mean(tool.pctChange(prices).take(3));

// shortMa
def shortMas = tool.ema(ohlcvs, 20);
def shortMa = shortMas.first();
def shortMaPctChange = tool.mean(tool.pctChange(shortMas).take(3));

// longMa
def longMas = tool.ema(ohlcvs, 60);
def longMa = longMas.first();
def longMaPctChange = tool.mean(tool.pctChange(longMas).take(3));

// min-max price
def minPrice = tool.min(prices.take(60));
def maxPrice = tool.max(prices.take(60));

// logging
log.info("== [{}] price:{}({}%), shortMa:{}({}%), longMa:{}({}%)", name, price, pricePctChange, shortMa, shortMaPctChange, longMa, longMaPctChange);
log.info("== [{}] minPrice:{}, maxPrice:{}", name, minPrice, maxPrice);

// buy
if((price > shortMa && shortMa > longMa)
&& (pricePctChange > 0.0 && shortMaPctChange > 0.0)
&& (price > maxPrice)
){
    hold = true;
}

// sell
if((price < shortMa)
&& (pricePctChange < -0.0)
&& (price < minPrice)
){
    hold = false;
}

// return
log.info("== [{}] hold:{}", name, hold);
return hold;

