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
    def pricePctChange = tool.sum(tool.pctChanges(prices).take(3));
    def volumes = ohlcvs.collect{it.volume};
    def volume = volumes.first();
    def volumePctChange = tool.sum(tool.pctChanges(volumes).take(3));
    log.debug("[{}] ohlcv: {}", name, ohlcv);
    log.debug("[{}] price: {}({}%)", name, price, pricePctChange);
    log.debug("[{}] volume: {}({}%)", name, volume, volumePctChange);
    log.debug("[{}] {}", name, tool.graph("Prices", prices));
    log.debug("[{}] {}", name, tool.graph("Volumes", volumes));

    // priceMa
    def priceMas = indicator.calculate(ohlcvType, period, SmaContext.DEFAULT);
    def priceMa = priceMas.first();
    def priceMaValues = priceMas.collect{it.value};
    def priceMaValue = priceMaValues.first();
    def priceMaValuePctChange = tool.sum(tool.pctChanges(priceMaValues).take(3));
    log.debug("[{}] priceMa: {}", name, priceMa);
    log.debug("[{}] priceMaValue: {}({}%)", name, priceMaValue, priceMaValuePctChange);
    log.debug("[{}] {}", name, tool.graph("PriceMaValues", priceMaValues));

    // macd
    def macds = indicator.calculate(ohlcvType, period, MacdContext.DEFAULT);
    def macd = macds.first();
    def macdValues = macds.collect{it.value};
    def macdValue = macdValues.first();
    def macdValuePctChange = tool.mean(tool.pctChanges(macdValues).take(3));
    def macdOscillators = macds.collect{it.oscillator};
    def macdOscillator = macdOscillators.first();
    def macdOscillatorPctChange = tool.sum(tool.pctChanges(macdOscillators).take(3));
    log.debug("[{}] macd: {}", name, macd);
    log.debug("[{}] macdValue: {}({}%)", name, macdValue, macdValuePctChange);
    log.debug("[{}] macdOscillator: {}({}%)", name, macdOscillator, macdOscillatorPctChange);
    log.debug("[{}] {}", name, tool.graph("MacdValues", macdValues));

    // rsi
    def rsis = indicator.calculate(ohlcvType, period, RsiContext.DEFAULT);
    def rsi = rsis.first();
    def rsiValues = rsis.collect{it.value};
    def rsiValue = rsiValues.first();
    def rsiValuePctChange = tool.sum(tool.pctChanges(rsiValues).take(3));
    log.debug("[{}] rsi: {}", name, rsi);
    log.debug("[{}] rsiValue: {}({}%)", name, rsiValue, rsiValuePctChange);
    log.debug("[{}] {}", name, tool.graph("RsiValues", rsiValues));

    // dmi
    def dmis = indicator.calculate(ohlcvType, period, DmiContext.DEFAULT);
    def dmi = dmis.first();
    def dmiPdis = dmis.collect{it.pdi};
    def dmiPdi = dmiPdis.first();
    def dmiPdiPctChange = tool.sum(tool.pctChanges(dmiPdis).take(3));
    def dmiMdis = dmis.collect{it.mdi};
    def dmiMdi = dmiMdis.first();
    def dmiMdiPctChange = tool.sum(tool.pctChanges(dmiMdis).take(3));
    def dmiAdxs = dmis.collect{it.adx};
    def dmiAdx = dmiAdxs.first();
    def dmiAdxPctChange = tool.sum(tool.pctChanges(dmiAdxs).take(3));
    log.debug("[{}] dmi: {}", name, dmi);
    log.debug("[{}] dmiPdiValue: {}({}%)", name, dmiPdi, dmiPdiPctChange);
    log.debug("[{}] dmiMidValue: {}({}%)", name, dmiMdi, dmiMdiPctChange);
    log.debug("[{}] dmiAdxValue: {}({}%)", name, dmiAdx, dmiAdxPctChange);
    log.debug("[{}] {}", name, tool.graph("DMI Pdi", dmiPdis));
    log.debug("[{}] {}", name, tool.graph("DMI Mdi", dmiMdis));

    // obv
    def obvs = indicator.calculate(ohlcvType, period, ObvContext.DEFAULT);
    def obv = obvs.first();
    def obvValues = obvs.collect{it.value};
    def obvValue = obvValues.first();
    def obvValuePctChange = tool.sum(tool.pctChanges(obvValues).take(3));
    log.debug("[{}] obv:{}", name, obv);
    log.debug("[{}] obvValue: {}({}%)", name, obvValue, obvValuePctChange);
    log.debug("[{}] {}", name, tool.graph("OBV Values", obvValues));

    // ad
    def ads = indicator.calculate(ohlcvType, period, AdContext.DEFAULT);
    def ad = ads.first();
    def adValues = ads.collect{it.value};
    def adValue = adValues.first();
    def adValuePctChange = tool.sum(tool.pctChanges(adValues).take(3));
    log.debug("[{}] ad: {}", name, ad);
    log.debug("[{}] adValue: {}({}%)", name, adValue, adValuePctChange);
    log.debug("[{}] {}", name, tool.graph("AD Values", adValues));

    // wvad
    def wvads = indicator.calculate(ohlcvType, period, WvadContext.DEFAULT);
    def wvad = wvads.first();
    def wvadValues = wvads.collect{it.value};
    def wvadValue = wvads.first();
    def wvadValuePctChange = tool.sum(tool.pctChanges(wvadValues).take(3));
    log.debug("[{}] wvad: {}", name, wvad);
    log.debug("[{}] wvadValue: {}({}%)", name, wvadValue, wvadValuePctChange);
    log.debug("[{}] {}", name, tool.graph("WVAD Values", wvadValues));

    // vote
    def vote = [:];
    vote.priceUp = (pricePctChange > 0.0 ? 100 : 0);
    vote.priceVolumeUp = (pricePctChange > 0.0 && volumePctChange > 0.0 ? 100 : 0);
    vote.priceOverPriceMa = (price > priceMaValue ? 100 : 0);
    vote.priceMaUp = (priceMaValuePctChange > 0.0 ? 100 : 0);
    vote.macdValue = (macdValue > 0 ? 100 : 0);
    vote.macdValueUp = (macdValuePctChange > 0.0 ? 100 : 0);
    vote.macdOscillator = (macdOscillator > 0 ? 100 : 0);
    vote.macdOscillatorPctChange = (macdOscillatorPctChange > 0.0 ? 100 : 0);
    vote.rsiOver50 = (rsiValue > 50 ? 100 : 0);
    vote.rsiUp = (rsiValuePctChange > 0.0 ? 100 :0);
    vote.dmiPdiOverMdi = (dmiPdi > dmiMdi ? 100 : 0);
    vote.dmiPdiUp = (dmiPdiPctChange > 0.0 ? 100 : 0);
    vote.dmiMdiDown = (dmiMdiPctChange < 0.0 ? 100 : 0);
    vote.dmiPdiAdxUp = (dmiPdiPctChange > 0.0 && dmiAdxPctChange > 0.0 ? 100 : 0);
    vote.obvValueUp = (obvValuePctChange > 0.0 ? 100 : 0);
    vote.adValueUp = (adValuePctChange > 0.0 ? 100 : 0);
    vote.wvadValueUp = (wvadValuePctChange > 0.0 ? 100 : 0);

    // return
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
