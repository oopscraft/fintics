package org.oopscraft.fintics.trade

import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.*

def analyze(Indicator indicator, OhlcvType ohlcvType, int period) {
    // info
    def name = indicator.getName() + ':' + ohlcvType + ':' + period

    // shortMa
    def shortMas = indicator.calculate(ohlcvType, period, EmaContext.of(10))
    def shortMa = shortMas.first()
    def shortMaValues = shortMas.collect{it.value}
    def shortMaValue = shortMaValues.first()
    def shortMaValuePcdChange = tool.pctChange(shortMaValues.take(10))
    log.debug("[{}] shortMa: {}", name, shortMa)

    // longMa
    def longMas = indicator.calculate(ohlcvType, period, EmaContext.of(30))
    def longMa = longMas.first()
    def longMaValues = longMas.collect{it.value}
    def longMaValue = longMaValues.first()
    def longMaValuePctChange = tool.pctChange(longMaValues.take(10))
    log.debug("[{}] longMa: {}", name, longMa)

    // macd
    def macds = indicator.calculate(ohlcvType, period, MacdContext.DEFAULT)
    def macd = macds.first()
    def macdValues = macds.collect{it.value}
    def macdValue = macdValues.first()
    def macdValuePctChange = tool.pctChange(macdValues.take(10));
    def macdSignals = macds.collect{it.signal}
    def macdSignal = macdSignals.first()
    def macdOscillators = macds.collect{it.oscillator}
    def macdOscillator = macdOscillators.first()
    log.debug("[{}] macd: {}", name, macd)

    // rsi
    def rsis = indicator.calculate(ohlcvType, period, RsiContext.DEFAULT)
    def rsi = rsis.first()
    def rsiValues = rsis.collect{it.value}
    def rsiValue = rsiValues.first()
    def rsiValuePctChange = tool.pctChange(rsiValues.take(10))
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
    result.shortMaValuePctChange = (shortMaValuePcdChange > 0.1 ? 100 : 0)
    result.longMaValuePctChange = (longMaValuePctChange > 0.1 ? 100 : 0)
    result.shortMaValueOverLongMaValue = (shortMaValue > longMaValue ? 100 : 0)
    result.macdValue = (macdValue > 0 ? 100 : 0)
    result.macdValuePctChange = (macdValuePctChange > 0.1 ? 100 : 0)
    result.macdOscillator = (macdOscillator > 0 ? 100 : 0)
    result.rsiValue = (rsiValue > 50 ? 100 : 0)
    result.rsiValueOverSignal = (rsiValue > rsiSignal ? 100 : 0)
    result.dmiPdiOverMdi = (dmiPdi > dmiMdi ? 100 : 0)
    result.dmiAdx = (dmiAdx > 25 ? 100 : 0);
    result.dmiAdx = (dmiPdi > dmiMdi && dmiAdx > 25 ? 100 : 0)
    result.obvValue = (obvValue > 0 ? 100 : 0)
    result.obvValueOverSignal = (obvValue > obvSignal ? 100 : 0)
    result.coValueOverSignal = (coValue > coSignal ? 100 : 0)

    // return
    return result
}

// defines
def assetName = assetIndicator.getName()
def holdVotes = []

// minute 10
def resultOfMinute10 = analyze(assetIndicator, OhlcvType.MINUTE, 10)
holdVotes.addAll(resultOfMinute10.values())
log.debug("[{}] resultOfMinute10: {}", assetName, resultOfMinute10)
log.info("[{}] resultOfMinute10Average: {}", assetName, resultOfMinute10.values().average())

// daily
def resultOfDaily = analyze(assetIndicator, OhlcvType.DAILY, 1)
holdVotes.addAll(resultOfDaily.values())
log.debug("[{}] resultOfDaily: {}", assetName, resultOfDaily)
log.info("[{}] resultOfDailyAverage: {}", assetName, resultOfDaily.values().average())

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
if(holdVotesAverage < 50) {
    hold = false
}

// return
return hold
