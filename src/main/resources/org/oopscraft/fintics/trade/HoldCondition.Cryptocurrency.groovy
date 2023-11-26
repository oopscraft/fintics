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
    def pricePctChange = tool.sum(tool.pctChanges(prices.take(3)));;
    def volumes = ohlcvs.collect{it.volume};
    def volume = volumes.first();
    def volumePctChange = tool.sum(tool.pctChanges(volumes.take(3)));
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

// defines
def assetName = assetIndicator.getName();
def holdVotes = [];

// minute 1
def holdVoteByMinute1 = getHoldVoteBy(assetIndicator, OhlcvType.MINUTE, 1);
holdVotes.addAll(holdVoteByMinute1.values());
log.debug("[{}] holdVoteByMinute1: {}", assetName, holdVoteByMinute1);
log.info("[{}] holdVoteByMinute1Average: {}", assetName, holdVoteByMinute1.values().average());

// minute 10
def holdVoteByMinute10 = getHoldVoteBy(assetIndicator, OhlcvType.MINUTE, 10);
holdVotes.addAll(holdVoteByMinute10.values());
log.debug("[{}] holdVoteByMinute10: {}", assetName, holdVoteByMinute10);
log.info("[{}] holdVoteByMinute10Average: {}", assetName, holdVoteByMinute10.values().average());

// minute 30
def holdVoteByMinute30 = getHoldVoteBy(assetIndicator, OhlcvType.MINUTE, 30);
holdVotes.addAll(holdVoteByMinute30.values());
log.debug("[{}] holdVoteByMinute30: {}", assetName, holdVoteByMinute30);
log.info("[{}] holdVoteByMinute30Average: {}", assetName, holdVoteByMinute30.values().average());

// minute 60
def holdVoteByMinute60 = getHoldVoteBy(assetIndicator, OhlcvType.MINUTE, 60);
holdVotes.addAll(holdVoteByMinute60.values());
log.debug("[{}] holdVoteByMinute60: {}", assetName, holdVoteByMinute60);
log.info("[{}] holdVoteByMinute60Average: {}", assetName, holdVoteByMinute60.values().average());

// decide hold
def hold = null;
def holdVotesAverage = holdVotes.average();
log.debug("[{}] holdVotes: {}", assetName, holdVotes);
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
