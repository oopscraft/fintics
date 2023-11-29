package org.oopscraft.fintics.trade

import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.Indicator
import org.oopscraft.fintics.model.OhlcvType

def analyze(Indicator indicator, OhlcvType ohlcvType, int period) {
    // info
    def name = indicator.getName() + ':' + ohlcvType + ':' + period

    // shortMa
    def shortMas = indicator.calculate(ohlcvType, period, EmaContext.of(10))
    def shortMa = shortMas.first()
    def shortMaValues = shortMas.collect{it.value}
    def shortMaValue = shortMaValues.first()
    def shortMaValuePctChange = tool.sum(tool.pctChanges(shortMaValues).take(3))
    log.debug("[{}] shortMa: {}", name, shortMa)
    log.debug("[{}] shortMaValue: {}({}%)", name, shortMaValue, shortMaValuePctChange)
    log.debug("[{}] {}", name, tool.graph("PriceMaValues", shortMaValues))

    // longMa
    def longMas = indicator.calculate(ohlcvType, period, EmaContext.of(30))
    def longMa = longMas.first()
    def longMaValues = longMas.collect{it.value}
    def longMaValue = longMaValues.first()
    def longMaValuePctChange = tool.sum(tool.pctChanges(longMaValues).take(3))
    log.debug("[{}] longMa: {}", name, longMa)
    log.debug("[{}] longMaValue: {}({}%)", name, longMaValue, longMaValuePctChange)
    log.debug("[{}] {}", name, tool.graph("LongMaValues", longMaValues))

    // macd
    def macds = indicator.calculate(ohlcvType, period, MacdContext.DEFAULT)
    def macd = macds.first()
    def macdValues = macds.collect{it.value}
    def macdValue = macdValues.first()
    def macdValuePctChange = tool.mean(tool.pctChanges(macdValues).take(3))
    def macdOscillators = macds.collect{it.oscillator}
    def macdOscillator = macdOscillators.first()
    def macdOscillatorPctChange = tool.sum(tool.pctChanges(macdOscillators).take(3))
    log.debug("[{}] macd: {}", name, macd)
    log.debug("[{}] macdValue: {}({}%)", name, macdValue, macdValuePctChange)
    log.debug("[{}] macdOscillator: {}({}%)", name, macdOscillator, macdOscillatorPctChange)
    log.debug("[{}] {}", name, tool.graph("MacdValues", macdValues))

    // rsi
    def rsis = indicator.calculate(ohlcvType, period, RsiContext.DEFAULT)
    def rsi = rsis.first()
    def rsiValues = rsis.collect{it.value}
    def rsiValue = rsiValues.first()
    def rsiValuePctChange = tool.sum(tool.pctChanges(rsiValues).take(3))
    log.debug("[{}] rsi: {}", name, rsi)
    log.debug("[{}] rsiValue: {}({}%)", name, rsiValue, rsiValuePctChange)
    log.debug("[{}] {}", name, tool.graph("RsiValues", rsiValues))

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
    def obvs = indicator.calculate(ohlcvType, period, ObvContext.DEFAULT)
    def obv = obvs.first()
    def obvValues = obvs.collect{it.value}
    def obvValue = obvValues.first()
    def obvValuePctChange = tool.sum(tool.pctChanges(obvValues).take(3))
    log.debug("[{}] obv:{}", name, obv)
    log.debug("[{}] obvValue: {}({}%)", name, obvValue, obvValuePctChange)
    log.debug("[{}] {}", name, tool.graph("OBV Values", obvValues))

    // ad
    def ads = indicator.calculate(ohlcvType, period, AdContext.DEFAULT)
    def ad = ads.first()
    def adValues = ads.collect{it.value}
    def adValue = adValues.first()
    def adValuePctChange = tool.sum(tool.pctChanges(adValues).take(3))
    log.debug("[{}] ad: {}", name, ad)
    log.debug("[{}] adValue: {}({}%)", name, adValue, adValuePctChange)
    log.debug("[{}] {}", name, tool.graph("AD Values", adValues))

    // wvad
    def wvads = indicator.calculate(ohlcvType, period, WvadContext.DEFAULT)
    def wvad = wvads.first()
    def wvadValues = wvads.collect{it.value}
    def wvadValue = wvads.first()
    def wvadValuePctChange = tool.sum(tool.pctChanges(wvadValues).take(3))
    log.debug("[{}] wvad: {}", name, wvad)
    log.debug("[{}] wvadValue: {}({}%)", name, wvadValue, wvadValuePctChange)
    log.debug("[{}] {}", name, tool.graph("WVAD Values", wvadValues))

    // result
    def result = [:]
    result.shortMaValueUp = (shortMaValuePctChange > 0.0 ? 100 : 0)
    result.longMaValueUp = (longMaValuePctChange > 0.0 ? 100 : 0)
    result.shortMaValueOverLongMaValue = (shortMaValue > longMaValue ? 100 : 0)
    result.macdValue = (macdValue > 0 ? 100 : 0)
    result.macdValueUp = (macdValuePctChange > 0.0 ? 100 : 0)
    result.macdOscillator = (macdOscillator > 0 ? 100 : 0)
    result.macdOscillatorUp = (macdOscillatorPctChange > 0.0 ? 100 : 0)
    result.rsiValue = (rsiValue > 50 ? 100 : 0)
    result.rsiValueUp = (rsiValuePctChange > 0.0 ? 100 :0)
    result.dmiPdiOverMdi = (dmiPdi > dmiMdi ? 100 : 0)
    result.dmiPdiUp = (dmiPdiPctChange > 0.0 ? 100 : 0)
    result.dmiMdiDown = (dmiMdiPctChange < 0.0 ? 100 : 0)
    result.obvValueUp = (obvValuePctChange > 0.0 ? 100 : 0)
    result.adValueUp = (adValuePctChange > 0.0 ? 100 : 0)
    result.wvadValueUp = (wvadValuePctChange > 0.0 ? 100 : 0)

    // return
    return result
}

// defines
def assetName = tradeAssetIndicator.getName()
def holdVotes = []

// minute 1
def resultOfMinute1 = analyze(tradeAssetIndicator, OhlcvType.MINUTE, 1)
holdVotes.addAll(resultOfMinute1.values())
log.debug("[{}] resultOfMinute1: {}", assetName, resultOfMinute1)
log.info("[{}] resultOfMinute1Average: {}", assetName, resultOfMinute1.values().average())

// minute 3
def resultOfMinute3 = analyze(tradeAssetIndicator, OhlcvType.MINUTE, 3)
holdVotes.addAll(resultOfMinute3.values())
log.debug("[{}] resultOfMinute3: {}", assetName, resultOfMinute3)
log.info("[{}] resultOfMinute3Average: {}", assetName, resultOfMinute3.values().average())

// minute 5
def resultOfMinute5 = analyze(tradeAssetIndicator, OhlcvType.MINUTE, 5)
holdVotes.addAll(resultOfMinute5.values())
log.debug("[{}] resultOfMinute5: {}", assetName, resultOfMinute5)
log.info("[{}] resultOfMinute5Average: {}", assetName, resultOfMinute5.values().average())

// decide hold
def hold = null

def holdVotesAverage = holdVotes.average()
log.debug("[{}] holdVotes: {}", assetName, holdVotes)
log.info("[{}] holdVotesAverage: {}", assetName, holdVotesAverage)

// buy
if(holdVotesAverage > 70) {
    hold = true
}

// sell
if(holdVotesAverage < 55) {
    hold = false
}

// return
return hold
