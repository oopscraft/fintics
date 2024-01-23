package org.oopscraft.fintics.trade

import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.*

/**
 * analyze indicator
 * @param indicator indicator
 * @param ohlcvType ohlcvType
 * @param period period
 * @return result map
 */
def analyzeIndicator(Indicator indicator, OhlcvType ohlcvType, int period) {
    // info
    def name = indicator.getIndicatorName() + ':' + ohlcvType + ':' + period
    def pctChangePeriod = 5

    // shortMa
    def shortMas = indicator.calculate(ohlcvType, period, EmaContext.of(10))
    def shortMa = shortMas.first()
    def shortMaValues = shortMas.collect{it.value}
    def shortMaValue = shortMaValues.first()
    def shortMaValuePctChange = tool.pctChange(shortMaValues.take(pctChangePeriod))
    log.debug("[{}] shortMa: {}", name, shortMa)

    // longMa
    def longMas = indicator.calculate(ohlcvType, period, EmaContext.of(30))
    def longMa = longMas.first()
    def longMaValues = longMas.collect{it.value}
    def longMaValue = longMaValues.first()
    def longMaValuePctChange = tool.pctChange(longMaValues.take(pctChangePeriod))
    log.debug("[{}] longMa: {}", name, longMa)

    // macd
    def macds = indicator.calculate(ohlcvType, period, MacdContext.DEFAULT)
    def macd = macds.first()
    def macdValues = macds.collect{it.value}
    def macdValue = macdValues.first()
    def macdValuePctChange = tool.pctChange(macdValues.take(pctChangePeriod))
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
    def rsiValuePctChange = tool.pctChange(rsiValues.take(pctChangePeriod))
    def rsiSignals = rsis.collect{it.signal}
    def rsiSignal = rsiSignals.first()
    log.debug("[{}] rsi: {}", name, rsi)

    // dmi
    def dmis = indicator.calculate(ohlcvType, period, DmiContext.DEFAULT)
    def dmi = dmis.first()
    def dmiPdis = dmis.collect{it.pdi}
    def dmiPdi = dmiPdis.first()
    def dmiPdiPctChange = tool.pctChange(dmiPdis.take(pctChangePeriod))
    def dmiMdis = dmis.collect{it.mdi}
    def dmiMdi = dmiMdis.first()
    def dmiMdiPctChange = tool.pctChange(dmiMdis.take(pctChangePeriod))
    def dmiAdxs = dmis.collect{it.adx}
    def dmiAdx = dmiAdxs.first()
    def dmiAdxPctChange = tool.pctChange(dmiAdxs.take(pctChangePeriod))
    log.debug("[{}] dmi: {}", name, dmi)

    // obv
    def obvs = indicator.calculate(ohlcvType, period, ObvContext.DEFAULT)
    def obv = obvs.first()
    def obvValues = obvs.collect{it.value}
    def obvValue = obvValues.first()
    def obvValuePctChange = tool.pctChange(obvValues.take(pctChangePeriod))
    def obvSignals = obv.collect{it.signal}
    def obvSignal = obvSignals.first()
    log.debug("[{}] obv:{}", name, obv)

    // co
    def cos = indicator.calculate(ohlcvType, period, CoContext.DEFAULT)
    def co = cos.first()
    def coValues = cos.collect{it.value}
    def coValue = coValues.first()
    def coValuePctChange = tool.pctChange(coValues.take(pctChangePeriod))
    def coSignals = cos.collect{it.signal}
    def coSignal = coSignals.first()
    log.debug("[{}] co: {}", name, co)

    // result
    def result = [:]
    result.shortMaValuePctChange = (shortMaValuePctChange > 0 ? 100 : 0)
    result.longMaValuePctChange = (longMaValuePctChange > 0 ? 100 : 0)
    result.shortMaValueOverLongMaValue = (shortMaValue > longMaValue ? 100 : 0)
    result.macdValuePctChange = (macdValuePctChange > 0 ? 100 : 0)
    result.macdValue = (macdValue > 0 ? 100 : 0)
    result.macdValueOverSignal = (macdValue > macdSignal ? 100 : 0)
    result.macdOscillator = (macdOscillator > 0 ? 100 : 0)
    result.rsiValuePctChange = (rsiValuePctChange > 0 ? 100 : 0)
    result.rsiValueOverSignal = (rsiValue > rsiSignal ? 100 : 0)
    result.rsiValue = (rsiValue > 50 ? 100 : 0)
    result.dmiPdiPctChange = (dmiPdiPctChange > 0 ? 100 : 0)
    result.dmiMdiPctChange = (dmiMdiPctChange < 0 ? 100 : 0)
    result.dmiPdiOverMdi = (dmiPdi > dmiMdi ? 100 : 0)
    result.dmiAdxPctChange = (dmiAdxPctChange > 0 && dmiPdiPctChange > 0 ? 100 : 0)
    result.dmiAdx = (dmiAdx > 20 && dmiPdi > dmiMdi ? 100 : 0)
    result.obvValuePctChange = (obvValuePctChange > 0 ? 100 : 0)
    result.obvValueOverSignal = (obvValue > obvSignal ? 100 : 0)
    result.coValuePctChange = (coValuePctChange > 0 ? 100 : 0)
    result.coValueOverSignal = (coValue > coSignal ? 100 : 0)
    result.coValue = (coValue > 0 ? 100 : 0)

    // return
    return result
}

//=============================
// defines
//=============================
def hold = null
def assetId = assetIndicator.getAssetId()
def assetName = assetIndicator.getAssetName()
def assetAlias = "${assetName}(${assetId})"
def analysisScores = []

//=============================
// analyze asset
//=============================
def assetResultMap = [:]
assetResultMap.minute3 = analyzeIndicator(assetIndicator, OhlcvType.MINUTE, 5)
assetResultMap.minute10 = analyzeIndicator(assetIndicator, OhlcvType.MINUTE, 10)
assetResultMap.minute30 = analyzeIndicator(assetIndicator, OhlcvType.MINUTE, 30)
assetResultMap.minute60 = analyzeIndicator(assetIndicator, OhlcvType.MINUTE, 60)
assetResultMap.daily = analyzeIndicator(assetIndicator, OhlcvType.DAILY, 1)
assetResultMap.each { key, value ->
    def average = value.values().average()
    analysisScores.add(average)
    log.debug("[{}] assetResult.{}: {}", assetAlias, key, average)
}

//=============================
// analyze indice
//=============================
// KOSPI
def kospiFutureResult = analyzeIndicator(indiceIndicators['KOSPI'], OhlcvType.MINUTE, 60)
analysisScores.add(kospiFutureResult.values().average())
log.debug("[{}] kospiFutureResult: {}", assetAlias, kospiFutureResult.values().average())

//=============================
// decide hold
//=============================
def analysisScoreAverage = analysisScores.average()
log.info("[{}] analysisScoreAverage:{}", assetAlias, analysisScoreAverage)
def assetScores = assetResultMap.values().collect{it.values().average()}
log.info("[{}] assetScores:{}", assetAlias, assetScores)

// default
if(analysisScoreAverage > 70) {
    log.info("[{}] analysisScoreAverage over 70", assetAlias)
    hold = true
}
if(analysisScoreAverage < 50) {
    log.info("[{}] assetResult under 50", assetAlias)
    hold = false
}

//==============================
// return
//==============================
return hold
