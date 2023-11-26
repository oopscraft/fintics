import org.oopscraft.fintics.calculator.AdContext
import org.oopscraft.fintics.calculator.DmiContext
import org.oopscraft.fintics.calculator.ObvContext
import org.oopscraft.fintics.calculator.WvadContext
import org.oopscraft.fintics.model.Indicator
import org.oopscraft.fintics.model.OhlcvType
import org.oopscraft.fintics.calculator.MacdContext
import org.oopscraft.fintics.calculator.RsiContext
import org.oopscraft.fintics.calculator.SmaContext



def getHoldVoteBy(Indicator indicator, OhlcvType ohlcvType, int period) {
    // info
    def name = indicator.getName() + ':' + ohlcvType + ':' + period;

    // price, volume
    def ohlcvs = indicator.resample(ohlcvType, period);
    def ohlcv = ohlcvs.first();
    def prices = ohlcvs.collect{it.closePrice};
    def price = prices.first();
    def pricePctChange = tool.pctChanges(prices).first();
    def volumes = ohlcvs.collect{it.volume};
    def volume = volumes.first();
    def volumePctChange = tool.pctChanges(volumes).first();
    log.debug("[{}] ohlcv: {}", name, ohlcv);
    log.debug("[{}] price: {}({}%)", name, price, pricePctChange);
    log.debug("[{}] volume: {}({}%)", name, volume, volumePctChange);

    // priceMa
    def priceMas = indicator.calculate(ohlcvType, period, SmaContext.DEFAULT);
    def priceMa = priceMas.first();
    def priceMaValue = priceMa.value;
    def priceMaValuePctChange = tool.pctChanges(priceMas.collect{it.value}).first();
    log.debug("[{}] priceMa: {}", name, priceMa);
    log.debug("[{}] priceMaValue: {}({}%)", name, priceMaValue, priceMaValuePctChange);

    // macd
    def macds = indicator.calculate(ohlcvType, period, MacdContext.DEFAULT);
    def macd = macds.first();
    def macdValue = macd.value;
    def macdValuePctChange = tool.pctChanges(macds.collect{it.value}).first();
    def macdOscillator = macd.oscillator;
    log.debug("[{}] macd: {}", name, macd);
    log.debug("[{}] macdValue: {}({}%)", name, macdValue, macdValuePctChange);

    // rsi
    def rsis = indicator.calculate(ohlcvType, period, RsiContext.DEFAULT);
    def rsi = rsis.first();
    def rsiValue = rsi.value;
    def rsiValuePctChange = tool.pctChanges(rsis.collect{it.value}).first();
    log.debug("[{}] rsi: {}", name, rsi);
    log.debug("[{}] rsiValue: {}({}%)", name, rsiValue, rsiValuePctChange);

    // dmi
    def dmis = indicator.calculate(ohlcvType, period, DmiContext.DEFAULT);
    def dmi = dmis.first();
    def dmiPdi = dmi.pdi;
    def dmiPdiPctChange = tool.pctChanges(dmis.collect{it.pdi}).first();
    def dmiMdi = dmi.mdi;
    def dmiMdiPctChange = tool.pctChanges(dmis.collect{it.mdi}).first();
    def dmiAdx = dmi.adx;
    def dmiAdxPctChange = tool.pctChanges(dmis.collect{it.adx}).first();
    log.debug("[{}] dmi: {}", name, dmi);
    log.debug("[{}] dmiPdiValue: {}({}%)", name, dmiPdi, dmiPdiPctChange);
    log.debug("[{}] dmiMidValue: {}({}%)", name, dmiMdi, dmiMdiPctChange);
    log.debug("[{}] dmiAdxValue: {}({}%)", name, dmiAdx, dmiAdxPctChange);

    // obv
    def obvs = indicator.calculate(ohlcvType, period, ObvContext.DEFAULT);
    def obv = obvs.first();
    def obvValue = obv.value;
    def obvValuePctChange = tool.pctChanges(obvs.collect{it.value}).first();
    log.debug("[{}] obv:{}", name, obv);
    log.debug("[{}] obvValue: {}({}%)", name, obvValue, obvValuePctChange);

    // ad
    def ads = indicator.calculate(ohlcvType, period, AdContext.DEFAULT);
    def ad = ads.first();
    def adValue = ad.value;
    def adValuePctChange = tool.pctChanges(ads.collect{it.value}).first();
    log.debug("[{}] ad: {}", name, ad);
    log.debug("[{}] adValue: {}({}%)", name, adValue, adValuePctChange);

    // wvad
    def wvads = indicator.calculate(ohlcvType, period, WvadContext.DEFAULT);
    def wvad = wvads.first();
    def wvadValue = wvad.value;
    def wvadValuePctChange = tool.pctChanges(wvads.collect{it.value}).first();
    log.debug("[{}] wvad: {}", name, wvad);
    log.debug("[{}] wvadValue: {}({}%)", name, wvadValue, wvadValuePctChange);

    // vote
    def vote = [:];
    vote.priceUp = (pricePctChange > 0.0 ? 100 : 0);
    vote.priceVolumeUp = (pricePctChange > 0.0 && volumePctChange > 0.0 ? 100 : 0);
    vote.priceOverPriceMa = (price > priceMaValue ? 100 : 0);
    vote.priceMaUp = (priceMaValuePctChange > 0.0 ? 100 : 0);
    vote.macdUp = (macdValuePctChange > 0.0 ? 100 : 0);
    vote.rsiOver50 = (rsiValue > 50 ? 100 : 0);
    vote.rsiUp = (rsiValuePctChange > 0.0 ? 100 :0);
    vote.dmiPdiOverMdi = (dmiPdi > dmiMdi ? 100 : 0);
    vote.dmiPdiUp = (dmiPdiPctChange > 0.0 ? 100 : 0);
    vote.dmiMdiDown = (dmiMdiPctChange < 0.0 ? 100 : 0);
    vote.dmiPdiAdxUp = (dmiPdiPctChange > 0.0 && dmiAdxPctChange > 0.0 ? 100 : 0);
    vote.obvUp = (obvValuePctChange > 0.0 ? 100 : 0);

    return vote;
}

def assetName = assetIndicator.getName();
def holdVoteByMinute1 = getHoldVoteBy(assetIndicator, OhlcvType.MINUTE, 1);
log.info("[{}] holdVoteByMinute1: {}", assetName, holdVoteByMinute1);
def holdVoteByMinute10 = getHoldVoteBy(assetIndicator, OhlcvType.MINUTE, 10);
log.info("[{}] holdVoteByMinute10: {}", assetName, holdVoteByMinute10);
def holdVoteByMinute30 = getHoldVoteBy(assetIndicator, OhlcvType.MINUTE, 30);
log.info("[{}] holdVoteByMinute30: {}", assetName, holdVoteByMinute30);

def holdVotes = [];
holdVotes.addAll(holdVoteByMinute1.values());
holdVotes.addAll(holdVoteByMinute10.values());
holdVotes.addAll(holdVoteByMinute30.values());

// decide hold
def hold = null;
def holdVotesAverage = holdVotes.average();
log.info("[{}] holdVotes: {}", assetName, holdVotes);
log.info("[{}] holdVotesAverage: {}", assetName, holdVotesAverage);

// buy
if(holdVotesAverage > 70) {
    hold = true;
}

// sell
if(holdVotesAverage < 50) {
    hold = false;
}

// return
return hold;



/
//// macd
//def macds = assetIndicator.calculate(OhlcvType.MINUTE, 1, MacdContext.DEFAULT);
//def macd = macds.first();
//def macdValuePctChange = tool.pctChanges(macds.collect{it.value}).first();
//
//// rsi
//def rsis = assetIndicator.calculate(OhlcvType.MINUTE, 1, RsiContext.DEFAULT);
//def rsi = rsis.first();
//def rsiValuePctChange = tool.pctChanges(rsis.collect{it.value}).first();



//// OHLCV(기본 1분 데이터)
//def ohlcvs = tool.resample(assetIndicator.getMinuteOhlcvs(), 5);
//def ohlcv = ohlcvs.first();
//
//// price
//def prices = ohlcvs.collect{it.closePrice};
//def price = prices.first();
//def pricePctChange = tool.sum(tool.pctChange(prices).take(3));
//
//// volume
//def volumes = ohlcvs.collect{it.volume};
//def volume = volumes.first();
//def volumePctChange = tool.sum(tool.pctChange(volumes).take(3));
//
//// shortMa
//def shortMas = tool.sma(ohlcvs, 10);
//def shortMa = shortMas.first();
//def shortMaPctChange = tool.sum(tool.pctChange(shortMas).take(3));
//
//// longMa
//def longMas = tool.sma(ohlcvs, 30);
//def longMa = longMas.first();
//def longMaPctChange = tool.sum(tool.pctChange(longMas).take(3));
//
//// macd
//def macds = tool.macd(ohlcvs, 12, 26, 9);
//def macd = macds.first();
//def macdValuePctChange = tool.sum(tool.pctChange(macds.collect{it.value}).take(3));
//
//// rsi
//def rsis = tool.rsi(ohlcvs, 14);
//def rsi = rsis.first();
//def rsiPctChange = tool.sum(tool.pctChange(rsis).take(3));
//
//// dmi
//def dmis = tool.dmi(ohlcvs, 14);
//def dmi = dmis.first();
//def dmiPdiPctChange = tool.sum(tool.pctChange(dmis.collect{it.pdi}).take(3));
//def dmiMdiPctChange = tool.sum(tool.pctChange(dmis.collect{it.mdi}).take(3));
//
////def DMIS = assetIndicator.getDmi(OhlcvType.MINUTE, 14);
////def RSIS = assetIndicator.getRsi(OhlcvType.MINUTE, 15);
////def TEST = assetIndicator.getOhlcvs(OhlcvType.MINUTE, 3);
////assetIndicator.calculate(OhlcvType.MINUTE, CalculatorType.MACD(12,30,12)).collect{it.value};
//def smas = assetIndicator.calculate(OhlcvType.MINUTE, 10, SmaContext.DEFAULT);
//log.debug("== sma:{}", smas);
//
//log.debug("===== {}", OhlcvType.MINUTE);
//
//// obv
//def obvs = tool.obv(ohlcvs);
//def obv = obvs.first();
//def obvPctChange = tool.sum(tool.pctChange(obvs).take(3));
//
//// hold vote
//def holdVote = [:];
//holdVote.pricePctChange = (pricePctChange > 0.0 ? 100 : 0);
//holdVote.priceVolumePctChange = (pricePctChange > 0.0 && volumePctChange > 0.0 ? 100 : 0);
//holdVote.priceShortMa = (price > shortMa ? 100 : 0);
//holdVote.priceLongMa = (price > longMa ? 100 : 0);
//holdVote.shortMaLongMa = (shortMa > longMa ? 100 : 0);
//holdVote.shortMaPctChange = (shortMaPctChange > 0.0 ? 100 : 0);
//holdVote.longMaPctChange = (longMaPctChange > 0.0 ? 100 : 0);
//holdVote.macdValue = (macd.value > 0 ? 100 : 0);
//holdVote.macdValuePctChange = (macdValuePctChange > 0 ? 100 : 0);
//holdVote.macdOscillator = (macd.oscillator > 0 ? 100 : 0);
//holdVote.rsi = (rsi > 50 ? 100 : 0);
//holdVote.rsiPctChange = (rsiPctChange > 0.0 ? 100 : 0);
//holdVote.dmiPdi = (dmi.pdi > dmi.mdi ? 100 : 0);
//holdVote.dmiPdiPctChange = (dmiPdiPctChange > 0.0 ? 100 : 0);
//holdVote.dmiMdiPctChange = (dmiMdiPctChange < 0.0 ? 100 : 0);
//holdVote.dmiAdx = (dmi.adx > 25 && dmi.pdi - dmi.mdi > 10 ? 100 : 0);
//holdVote.obv = (obv > 0 ? 100 : 0);
//holdVote.obvPctChage = (obvPctChange > 0.0 ? 100 : 0);
//
//// hold vote result
//def holdVoteResult = holdVote.values()
//        .toList()
//        .average();
//
//// logging
//log.debug("[{}] orderBook:{}", name, orderBook);
//log.debug("[{}] ohlcv:{}", name, ohlcv);
//log.debug("[{}] price:{}({}%)", name, price, pricePctChange);
//log.debug("[{}] volume:{}({}%)", name, volume, volumePctChange);
//log.debug("[{}] shortMa:{}({}%)", name, shortMa, shortMaPctChange);
//log.debug("[{}] longMa:{}({}%)", name, longMa, longMaPctChange);
//log.debug("[{}] macd:{}", name, macd);
//log.debug("[{}] rsi:{}", name, rsi);
//log.debug("[{}] dmi:{}", name, dmi);
//log.debug("[{}] obv:{}({}%)", name, obv, obvPctChange);
//holdVote.each { key, value -> {
//    log.debug("[{}] holdVote[{}]:{}", name, key, value);
//}};
//log.debug("[{}] holdVoteResult:{}", name, holdVoteResult);
//
//// 매수 여부 판단
//if(pricePctChange > 0.0) {
//    if(holdVoteResult > 80) {
//        hold = true;
//    }
//}
//
//// 매도 여부 판단
//if(pricePctChange < 0.0) {
//    if(holdVoteResult < 50) {
//        hold = false;
//    }
//}
//
//// return
//log.debug("[{}] hold:{}", name, hold);
//return hold;
