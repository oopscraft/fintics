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
    def shortMaValueSlope = tool.slope(shortMaValues.take(3))
    log.debug("[{}] shortMa: {}", name, shortMa)
    log.debug("[{}] shortMaValue(Slope): {}({})", name, shortMaValue, shortMaValueSlope)

    // longMa
    def longMas = indicator.calculate(ohlcvType, period, EmaContext.of(30))
    def longMa = longMas.first()
    def longMaValues = longMas.collect{it.value}
    def longMaValue = longMaValues.first()
    def longMaValueSlope = tool.slope(longMaValues.take(3))
    log.debug("[{}] longMa: {}", name, longMa)
    log.debug("[{}] longMaValue(Slope): {}({})", name, longMaValue, longMaValueSlope)

    // macd
    def macds = indicator.calculate(ohlcvType, period, MacdContext.DEFAULT)
    def macd = macds.first()
    def macdValues = macds.collect{it.value}
    def macdValue = macdValues.first()
    def macdValueSlope = tool.slope(macdValues.take(3))
    def macdOscillators = macds.collect{it.oscillator}
    def macdOscillator = macdOscillators.first()
    def macdOscillatorSlope = tool.slope(macdOscillators.take(3))
    log.debug("[{}] macd: {}", name, macd)
    log.debug("[{}] macdValue(Slope): {}({})", name, macdValue, macdValueSlope)
    log.debug("[{}] macdOscillator(Slope): {}({})", name, macdOscillator, macdOscillatorSlope)

    // rsi
    def rsis = indicator.calculate(ohlcvType, period, RsiContext.DEFAULT)
    def rsi = rsis.first()
    def rsiValues = rsis.collect{it.value}
    def rsiValue = rsiValues.first()
    def rsiValueSlope = tool.slope(rsiValues.take(3))
    log.debug("[{}] rsi: {}", name, rsi)
    log.debug("[{}] rsiValue(Slope): {}({}%)", name, rsiValue, rsiValueSlope)

    // dmi
    def dmis = indicator.calculate(ohlcvType, period, DmiContext.DEFAULT);
    def dmi = dmis.first();
    def dmiPdis = dmis.collect{it.pdi}
    def dmiPdi = dmiPdis.first();
    def dmiPdiSlope = tool.slope(dmiPdis.take(3))
    def dmiMdis = dmis.collect{it.mdi}
    def dmiMdi = dmiMdis.first();
    def dmiMdiSlope = tool.slope(dmiMdis.take(3))
    def dmiAdxs = dmis.collect{it.adx}
    def dmiAdx = dmiAdxs.first()
    def dmiAdxSlope = tool.slope(dmiAdxs.take(3))
    log.debug("[{}] dmi: {}", name, dmi)
    log.debug("[{}] dmiPdi(Slope): {}({})", name, dmiPdi, dmiPdiSlope)
    log.debug("[{}] dmiMid(Slope): {}({})", name, dmiMdi, dmiMdiSlope)
    log.debug("[{}] dmiAdx(Slope): {}({})", name, dmiAdx, dmiAdxSlope)

    // obv
    def obvs = indicator.calculate(ohlcvType, period, ObvContext.DEFAULT)
    def obv = obvs.first()
    def obvValues = obvs.collect{it.value}
    def obvValue = obvValues.first()
    def obvValueSlope = tool.slope(obvValues.take(3))
    log.debug("[{}] obv:{}", name, obv)
    log.debug("[{}] obvValue(Slope): {}({})", name, obvValue, obvValueSlope)

    // ad
    def ads = indicator.calculate(ohlcvType, period, AdContext.DEFAULT)
    def ad = ads.first()
    def adValues = ads.collect{it.value}
    def adValue = adValues.first()
    def adValueSlope = tool.slope(adValues.take(3))
    log.debug("[{}] ad: {}", name, ad)
    log.debug("[{}] adValue(Slope): {}({})", name, adValue, adValueSlope)

    // wvad
    def wvads = indicator.calculate(ohlcvType, period, WvadContext.DEFAULT)
    def wvad = wvads.first()
    def wvadValues = wvads.collect{it.value}
    def wvadValue = wvads.first()
    def wvadValueSlope = tool.slope(wvadValues.take(3))
    log.debug("[{}] wvad: {}", name, wvad)
    log.debug("[{}] wvadValue(Slope): {}({})", name, wvadValue, wvadValueSlope)

    // result
    def result = [:]
    result.shortMaValueUp = (shortMaValueSlope > 0.0 ? 100 : 0)
    result.longMaValueUp = (longMaValueSlope > 0.0 ? 100 : 0)
    result.shortMaValueOverLongMaValue = (shortMaValue > longMaValue ? 100 : 0)
    result.macdValue = (macdValue > 0 ? 100 : 0)
    result.macdValueUp = (macdValueSlope > 0.0 ? 100 : 0)
    result.macdOscillator = (macdOscillator > 0 ? 100 : 0)
    result.macdOscillatorUp = (macdOscillatorSlope > 0.0 ? 100 : 0)
    result.rsiValue = (rsiValue > 50 ? 100 : 0)
    result.rsiValueUp = (rsiValueSlope > 0.0 ? 100 :0)
    result.dmiPdiOverMdi = (dmiPdi > dmiMdi ? 100 : 0)
    result.dmiPdiUp = (dmiPdiSlope > 0.0 ? 100 : 0)
    result.dmiMdiDown = (dmiMdiSlope < 0.0 ? 100 : 0)
    result.obvValueUp = (obvValueSlope > 0.0 ? 100 : 0)
    result.adValueUp = (adValueSlope > 0.0 ? 100 : 0)
    result.wvadValueUp = (wvadValueSlope > 0.0 ? 100 : 0)

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

// minute 10
def resultOfMinute10 = analyze(tradeAssetIndicator, OhlcvType.MINUTE, 10)
holdVotes.addAll(resultOfMinute10.values())
log.debug("[{}] resultOfMinute10: {}", assetName, resultOfMinute10)
log.info("[{}] resultOfMinute10Average: {}", assetName, resultOfMinute10.values().average())

// decide hold
def hold = null
def holdVotesAverage = holdVotes.average()
log.debug("[{}] holdVotes: {}", assetName, holdVotes)
log.info("[{}] holdVotesAverage: {}", assetName, holdVotesAverage)

// buy
if(holdVotesAverage > 75) {
    hold = true
}

// sell
if(holdVotesAverage < 50) {
    hold = false
}

// return
return hold
