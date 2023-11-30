package org.oopscraft.fintics.trade

import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.Indicator
import org.oopscraft.fintics.model.OhlcvType

import java.time.LocalTime

def analyze(Indicator indicator, OhlcvType ohlcvType, int period) {
    // info
    def name = indicator.getName() + ':' + ohlcvType + ':' + period
    def changeDetectPeriod = 5;

    // shortMa
    def shortMas = indicator.calculate(ohlcvType, period, EmaContext.of(10))
    def shortMa = shortMas.first()
    def shortMaValues = shortMas.collect{it.value}
    def shortMaValue = shortMaValues.first()
    def shortMaPctChange = tool.pctChange(shortMaValues.take(changeDetectPeriod))
    log.debug("[{}] shortMa: {}", name, shortMa)
    log.debug("[{}] shortMaValue(PctChange): {}({}%)", name, shortMaValue, shortMaPctChange)

    // longMa
    def longMas = indicator.calculate(ohlcvType, period, EmaContext.of(30))
    def longMa = longMas.first()
    def longMaValues = longMas.collect{it.value}
    def longMaValue = longMaValues.first()
    def longMaValuePctChange = tool.pctChange(longMaValues.take(changeDetectPeriod))
    log.debug("[{}] longMa: {}", name, longMa)
    log.debug("[{}] longMaValue(PctChange): {}({}%)", name, longMaValue, longMaValuePctChange)

    // macd
    def macds = indicator.calculate(ohlcvType, period, MacdContext.DEFAULT)
    def macd = macds.first()
    def macdValues = macds.collect{it.value}
    def macdValue = macdValues.first()
    def macdValuePctChange = tool.pctChange(macdValues.take(changeDetectPeriod))
    def macdOscillators = macds.collect{it.oscillator}
    def macdOscillator = macdOscillators.first()
    def macdOscillatorPctChange = tool.pctChange(macdOscillators.take(10))
    log.debug("[{}] macd: {}", name, macd)
    log.debug("[{}] macdValue(PctChange): {}({}%)", name, macdValue, macdValuePctChange)
    log.debug("[{}] macdOscillator(PctChange): {}({}%)", name, macdOscillator, macdOscillatorPctChange)

    // rsi
    def rsis = indicator.calculate(ohlcvType, period, RsiContext.DEFAULT)
    def rsi = rsis.first()
    def rsiValues = rsis.collect{it.value}
    def rsiValue = rsiValues.first()
    def rsiValuePctChange = tool.pctChange(rsiValues.take(changeDetectPeriod))
    log.debug("[{}] rsi: {}", name, rsi)
    log.debug("[{}] rsiValue(PctChange): {}({}%)", name, rsiValue, rsiValuePctChange)

    // dmi
    def dmis = indicator.calculate(ohlcvType, period, DmiContext.DEFAULT);
    def dmi = dmis.first();
    def dmiPdis = dmis.collect{it.pdi}
    def dmiPdi = dmiPdis.first();
    def dmiPdiPctChange = tool.pctChange(dmiPdis.take(changeDetectPeriod))
    def dmiMdis = dmis.collect{it.mdi}
    def dmiMdi = dmiMdis.first();
    def dmiMdiPctChange = tool.pctChange(dmiMdis.take(changeDetectPeriod))
    def dmiAdxs = dmis.collect{it.adx}
    def dmiAdx = dmiAdxs.first()
    def dmiAdxPctChange = tool.pctChange(dmiAdxs.take(changeDetectPeriod))
    log.debug("[{}] dmi: {}", name, dmi)
    log.debug("[{}] dmiPdi(PctChange): {}({}%)", name, dmiPdi, dmiPdiPctChange)
    log.debug("[{}] dmiMid(PctChange): {}({}%)", name, dmiMdi, dmiMdiPctChange)
    log.debug("[{}] dmiAdx(PctChange): {}({}%)", name, dmiAdx, dmiAdxPctChange)

    // obv
    def obvs = indicator.calculate(ohlcvType, period, ObvContext.DEFAULT)
    def obv = obvs.first()
    def obvValues = obvs.collect{it.value}
    def obvValue = obvValues.first()
    def obvValuePctChange = tool.pctChange(obvValues.take(changeDetectPeriod))
    log.debug("[{}] obv:{}", name, obv)
    log.debug("[{}] obvValue(PctChange): {}({}%)", name, obvValue, obvValuePctChange)

    // ad
    def ads = indicator.calculate(ohlcvType, period, AdContext.DEFAULT)
    def ad = ads.first()
    def adValues = ads.collect{it.value}
    def adValue = adValues.first()
    def adValuePctChange = tool.pctChange(adValues.take(changeDetectPeriod))
    log.debug("[{}] ad: {}", name, ad)
    log.debug("[{}] adValue(PctChange): {}({}%)", name, adValue, adValuePctChange)

    // wvad
    def wvads = indicator.calculate(ohlcvType, period, WvadContext.DEFAULT)
    def wvad = wvads.first()
    def wvadValues = wvads.collect{it.value}
    def wvadValue = wvads.first()
    def wvadValuePctChange = tool.pctChange(wvadValues.take(changeDetectPeriod))
    log.debug("[{}] wvad: {}", name, wvad)
    log.debug("[{}] wvadValue(PctChange): {}({}%)", name, wvadValue, wvadValuePctChange)

    // result
    def result = [:]
    result.shortMaValueUp = (shortMaPctChange > 0.0 ? 100 : 0)
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

// minute 10
def resultOfMinute10 = analyze(tradeAssetIndicator, OhlcvType.MINUTE, 10)
holdVotes.addAll(resultOfMinute10.values())
log.debug("[{}] resultOfMinute10: {}", assetName, resultOfMinute10)
log.info("[{}] resultOfMinute10Average: {}", assetName, resultOfMinute10.values().average())

// USD/KRW (환율 하락 시 매수)
def resultOfUsdKrw = analyze(indiceIndicators['USD_KRW'], OhlcvType.MINUTE, 10)
holdVotes.addAll(resultOfUsdKrw.values().collect{100 - (it as Number)})
log.debug("[{}] resultOfUsdKrw: {}", assetName, resultOfUsdKrw)
log.info("[{}] resultOfUsdKrwAverage: {}", assetName, resultOfUsdKrw.values().average())

// Nasdaq Future (나스닥 선물 상승 시 매수)
def resultOfNdxFuture = analyze(indiceIndicators['NDX_FUTURE'], OhlcvType.MINUTE, 10)
holdVotes.addAll(resultOfNdxFuture.values())
log.debug("[{}] resultOfNdxFuture: {}", assetName, resultOfNdxFuture)
log.info("[{}] resultOfNdxFutureAverage: {}", assetName, resultOfNdxFuture.values().average())

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

// 장종료 전 매도 (보유 하지 않음)
if(dateTime.toLocalTime().isAfter(LocalTime.of(15, 15))) {
    hold = false
}

// return
return hold
