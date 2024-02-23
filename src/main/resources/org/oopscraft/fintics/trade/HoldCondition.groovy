package org.oopscraft.fintics.trade

import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.*

import java.time.LocalDate
import java.time.LocalTime

class Analysis {
    List<Ohlcv> ohlcvs
    Ohlcv ohlcv
    List<Ema> shortEmas
    Ema shortEma
    List<Ema> longEmas
    Ema longEma
    List<Macd> macds
    Macd macd
    List<Rsi> rsis
    Rsi rsi
    List<Dmi> dmis
    Dmi dmi
    List<Obv> obvs
    Obv obv
    List<Co> cos
    Co co
}

static def getAnalysis(Indicator indicator, Ohlcv.Type ohlcvType, int ohlcvPeriod) {
    def analysis = new Analysis()
    // ohlcv
    analysis.ohlcvs = indicator.getOhlcvs(ohlcvType, ohlcvPeriod)
    analysis.ohlcv = analysis.ohlcvs.first()
    // shorEma
    analysis.shortEmas = indicator.calculate(EmaContext.of(10), ohlcvType, ohlcvPeriod)
    analysis.shortEma = analysis.shortEmas.first()
    // longEma
    analysis.longEmas = indicator.calculate(EmaContext.of(20), ohlcvType, ohlcvPeriod)
    analysis.longEma = analysis.longEmas.first()
    // macd
    analysis.macds = indicator.calculate(MacdContext.DEFAULT, ohlcvType, ohlcvPeriod)
    analysis.macd = analysis.macds.first()
    // rsi
    analysis.rsis = indicator.calculate(RsiContext.DEFAULT, ohlcvType, ohlcvPeriod)
    analysis.rsi = analysis.rsis.first()
    // dmi
    analysis.dmis = indicator.calculate(DmiContext.DEFAULT, ohlcvType, ohlcvPeriod)
    analysis.dmi = analysis.dmis.first()
    // obv
    analysis.obvs = indicator.calculate(ObvContext.DEFAULT, ohlcvType, ohlcvPeriod)
    analysis.obv = analysis.obvs.first()
    // co
    analysis.cos = indicator.calculate(CoContext.DEFAULT, ohlcvType, ohlcvPeriod)
    analysis.co = analysis.cos.first()
    // return
    return analysis
}

// dateTime.toLocalTime().getHour()

def hold = null
def ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1).findAll{it.dateTime.toLocalDate().equals(dateTime.toLocalDate())}
def period = 30 //ohlcvs.size()
def prices = ohlcvs.collect{it.lowPrice}
def priceZScore = tool.zScore(prices.take(period))
def volumes = ohlcvs.collect{it.volume}
def volumeZScore = tool.zScore(volumes.take(period))

// macd
def macds = tool.calculate(ohlcvs, MacdContext.DEFAULT)
def macd = macds.first()
def macdValue = macd.value
def macdSignal = macd.signal
def macdOscillator = macd.oscillator
log.info("macd: {}", macd)



if (priceZScore > 1.5) {
    hold = 1
}
if (priceZScore < 0.0) {
    hold = 0
}

if(dateTime.toLocalTime().isBefore(LocalTime.of(9,10))) {
    hold = null
}

if (dateTime.toLocalTime().isAfter(LocalTime.of(12,0))) {
    if (hold == 1) {
        hold = null
    }
}

if (dateTime.toLocalTime().isAfter(LocalTime.of(15,10))) {
    hold = 0
}

return hold

//static def getMomentumScores(Analysis analysis) {
//    def scores = [:]
//    // ema
//    scores.emaShortOverLong = analysis.shortEma.value > analysis.longEma.value ? 100 : 0
//    // macd
//    scores.maceValue = analysis.macd.value > 0 ? 100 : 0
//    scores.macdValueOverSignal = analysis.macd.value > analysis.macd.signal ? 100 : 0
//    scores.macdOscillator = analysis.macd.oscillator > 0 ? 100 : 0
//    // rsi
//    scores.rsiValue = analysis.rsi.value > 50 ? 100 : 0
//    scores.rsiValueOverSignal = analysis.rsi.value > analysis.rsi.signal ? 100 : 0
//    // dmi
//    scores.dmiPdiOverMdi = analysis.dmi.pdi > analysis.dmi.mdi ? 100 : 0
//    scores.dmiAdx = analysis.dmi.adx > 25 && analysis.dmi.pdi > analysis.dmi.mdi ? 100 : 0
//    // obv
//    scores.obvValueOverSignal = analysis.obv.value > analysis.obv.signal ? 100 : 0
//    // co
//    scores.coValue = analysis.co.value > 0 ? 100 : 0
//    scores.coValueOverSignal = analysis.co.value > analysis.co.signal ? 100 : 0
//    // return
//    return scores
//}
//
////def getDirectionScores(Analysis analysis) {
////    def scores = getMomentumScores(analysis)
////    def prices = analysis.ohlcvs.collect{it.closePrice}
////    def price = prices.first()
////    scores.pricePctChange = tool.pctChange(prices.take(5)) > 0.1 ? 100 : 0
////    scores.priceOverShortEma = price > analysis.shortEma.value ? 100 : 0
////    // return
////    return scores
////}
//
//// define
//def hold = null
//def assetId = assetIndicator.getAssetId()
//def assetName = assetIndicator.getAssetName()
//def assetAlias = "${assetName}(${assetId})"
//def time = dateTime.toLocalTime()
//
//// analysis
////def analysisMinute = getAnalysis(assetIndicator, Ohlcv.Type.MINUTE, 1)
//def analysisMinute5 = getAnalysis(assetIndicator, Ohlcv.Type.MINUTE, 1)
////def analysisMinute10 = getAnalysis(assetIndicator, Ohlcv.Type.MINUTE, 10)
////def analysisDaily = getAnalysis(assetIndicator, Ohlcv.Type.DAILY, 1)
//
//// analysis score
//def analysisScoresMinute5 = getMomentumScores(analysisMinute5)
////def analysisScoresMinute10 = getMomentumScores(analysisMinute10)
////def analysisScoresDaily = getMomentumScores(analysisDaily)
//def analysisScoreMinute5 = analysisScoresMinute5.values().average()
////def analysisScoreMinute10 = analysisScoresMinute10.values().average()
////def analysisScoreDaily = analysisScoresDaily.values().average()
////log.info("[{}] momentumScoresMinute5: {}", assetAlias, analysisScoresMinute60)
////log.info("[{}] momentumScoresDaily: {}", assetAlias,  anallysisScoresDaily)
////log.info("[{}] momentumScore: {}", assetAlias, momentumScore)
//
//// default buy
//if (analysisScoreMinute5 > 70) {
//    hold = 1
//}
//
//// default sell
//if (analysisScoreMinute5 < 60) {
//    hold = 0
//}

// middle term
if (time.isAfter(LocalTime.of(12,00))) {
//    if (analysisScoreMinute5 > 70) {
       hold = null
//    }
    if (analysisScoreMinute5 < 60) {
        hold = 0
    }
}

// middle term
//if (time.isAfter(LocalTime.of(2,30))) {
//    if (analysisScoreMinute5 > 70) {
//       hold = 1
//    }
//    if (analysisScoreMinute5 < 60) {
//        hold = 0
//    }
//}

// post process
if (time.isAfter(LocalTime.of(15,15))) {
    hold = 0
//    if(analysisScoreDaily > 70) {
//        hold = 1
//    }
//    if(analysisScoreDaily < 60) {
//        hold = 0
//    }
}

// return
return hold

