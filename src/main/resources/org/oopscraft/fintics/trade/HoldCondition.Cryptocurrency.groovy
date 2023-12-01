package org.oopscraft.fintics.trade

import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.*

def analyze(OhlcvType ohlcvType, int period) {
    // info
    def name = indicator.getName() + ':' + ohlcvType + ':' + period

    // shortMa
    def shortMas = indicator.calculate(ohlcvType, period, EmaContext.of(10))
    def shortMa = shortMas.first()
    def shortMaValues = shortMas.collect{it.value}
    def shortMaValue = shortMaValues.first()
    log.debug("[{}] shortMa: {}", name, shortMa)

    // longMa
    def longMas = indicator.calculate(ohlcvType, period, EmaContext.of(30))
    def longMa = longMas.first()
    def longMaValues = longMas.collect{it.value}
    def longMaValue = longMaValues.first()
    log.debug("[{}] longMa: {}", name, longMa)

    // macd
    def macds = indicator.calculate(ohlcvType, period, MacdContext.DEFAULT)
    def macd = macds.first()
    def macdValues = macds.collect{it.value}
    def macdValue = macdValues.first()
    def macdOscillators = macds.collect{it.oscillator}
    def macdOscillator = macdOscillators.first()
    log.debug("[{}] macd: {}", name, macd)

    // rsi
    def rsis = indicator.calculate(ohlcvType, period, RsiContext.DEFAULT)
    def rsi = rsis.first()
    def rsiValues = rsis.collect{it.value}
    def rsiValue = rsiValues.first()
    def rsiSignals = rsis.collect{it.signal}
    def rsiSignal = rsiSignals.first()
    log.debug("[{}] rsi: {}", name, rsi)

    // dmi
    def dmis = indicator.calculate(ohlcvType, period, DmiContext.DEFAULT);
    def dmi = dmis.first();
    def dmiPdis = dmis.collect{it.pdi}
    def dmiPdi = dmiPdis.first();
    def dmiMdis = dmis.collect{it.mdi}
    def dmiMdi = dmiMdis.first();
    def dmiAdxs = dmis.collect{it.adx}
    def dmiAdx = dmiAdxs.first()
    log.debug("[{}] dmi: {}", name, dmi)

    // obv
    def obvs = indicator.calculate(ohlcvType, period, ObvContext.DEFAULT)
    def obv = obvs.first()
    def obvValues = obvs.collect{it.value}
    def obvValue = obvValues.first()
    def obvSignals = obv.collect{it.signal}
    def obvSignal = obvSignals.first();
    log.debug("[{}] obv:{}", name, obv)

    // co
    def cos = indicator.calculate(ohlcvType, period, CoContext.DEFAULT)
    def co = cos.first()
    def coValues = cos.collect{it.value}
    def coValue = coValues.first()
    def coSignals = cos.collect{it.signal}
    def coSignal = coSignals.first()
    log.debug("[{}] co: {}", name, co)

    // result
    def result = [:]
    result.shortMaValueOverLongMaValue = (shortMaValue > longMaValue ? 100 : 0)
    result.macdValue = (macdValue > 0 ? 100 : 0)
    result.macdOscillator = (macdOscillator > 0 ? 100 : 0)
    result.rsiValue = (rsiValue > 50 ? 100 : 0)
    result.rsiValueOverSignal = (rsiValue > rsiSignal ? 100 : 0)
    result.dmiPdiOverMdi = (dmiPdi > dmiMdi ? 100 : 0)
    result.dmiAdx = (dmiPdi > dmiMdi && dmiAdx > 25 ? 100 : 0)
    result.obvValueOverSignal = (obvValue > obvSignal ? 100 : 0)
    result.coValue = (coValue > 0 ? 100 : 0)
    result.coValueOverSignal = (coValue > coSignal ? 100 : 0)

    // return
    return result
}

// defines
def assetName = indicator.getName()
def holdVotes = []

// minute 1
def resultOfMinute1 = analyze(OhlcvType.MINUTE, 1)
holdVotes.addAll(resultOfMinute1.values())
log.debug("[{}] resultOfMinute1: {}", assetName, resultOfMinute1)
log.info("[{}] resultOfMinute1Average: {}", assetName, resultOfMinute1.values().average())

// minute 3
def resultOfMinute3 = analyze(OhlcvType.MINUTE, 3)
holdVotes.addAll(resultOfMinute3.values())
log.debug("[{}] resultOfMinute3: {}", assetName, resultOfMinute3)
log.info("[{}] resultOfMinute3Average: {}", assetName, resultOfMinute3.values().average())

// minute 5
def resultOfMinute5 = analyze(OhlcvType.MINUTE, 5)
holdVotes.addAll(resultOfMinute5.values())
log.debug("[{}] resultOfMinute5: {}", assetName, resultOfMinute5)
log.info("[{}] resultOfMinute5Average: {}", assetName, resultOfMinute5.values().average())

// decide hold
def hold = null
def holdVotesAverage = holdVotes.average()
log.debug("[{}] holdVotes: {}", assetName, holdVotes)
log.info("[{}] holdVotesAverage: {}", assetName, holdVotesAverage)

// buy
if(holdVotesAverage > 80) {
    hold = true
}

// sell
if(holdVotesAverage < 60) {
    hold = false
}

// return
return hold
