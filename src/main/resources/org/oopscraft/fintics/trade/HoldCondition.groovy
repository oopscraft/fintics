import groovy.transform.ToString
import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.*

import java.math.RoundingMode

/**
 * analysis
 */
@ToString(includeNames = true)
class Analysis {
    List<Ohlcv> ohlcvs
    List<Macd> macds
    List<Rsi> rsis
    List<Dmi> dmis
    List<Obv> obvs
    List<Co> cos
}

/**
 * return analysis
 * @param ohlcvs
 * @return analysis
 */
def getAnalysis(List<Ohlcv> ohlcvs) {
    def analysis = new Analysis()
    analysis.ohlcvs = ohlcvs
    analysis.macds = tool.calculate(ohlcvs, MacdContext.DEFAULT)
    analysis.rsis = tool.calculate(ohlcvs, RsiContext.DEFAULT)
    analysis.dmis = tool.calculate(ohlcvs, DmiContext.DEFAULT)
    analysis.obvs = tool.calculate(ohlcvs, ObvContext.DEFAULT)
    analysis.cos = tool.calculate(ohlcvs, CoContext.DEFAULT)
    return analysis
}

interface Score {}

/**
 * momentum score
 */
@ToString(includeNames = true)
class MomentumScore implements Score {
    def macdValue
    def macdSignal
    def macdValueOverSignal
    def macdOscillator
    def rsiValue
    def rsiSignal
    def rsiValueOverSignal
    def dmiPdiOverMdi
    def dmiAdx
    def obvValueOverSignal
    def coValue
    def coSignal
}

/**
 * return momentum score
 * @param analysis
 * @return momentum score
 */
def getMomentumScore(Analysis analysis) {
    def momentumScore = new MomentumScore()
    def period = 5
    // macd
    def macds = analysis.macds.take(period)
    def macd = macds.first()
    momentumScore.macdValue = tool.pctChange(macds.collect{it.value}) > 0 ? 100 : 0
    momentumScore.macdSignal = tool.pctChange(macds.collect{it.signal}) > 0 ? 100 : 0
    momentumScore.macdOscillator = tool.pctChange(macds.collect{it.oscillator}) > 0 ? 100 : 0
    momentumScore.macdValueOverSignal = macd.value > macd.oscillator ? 100 : 0
//    // rsi
//    def rsis = analysis.rsis.take(period)
//    def rsi = rsis.first()
//    momentumScore.rsiValue = tool.pctChange(rsis.collect{it.value}) > 50 ? 100 : 0
//    momentumScore.rsiSignal = tool.pctChange(rsis.collect{it.signal}) > 50 ? 100 : 0
//    momentumScore.rsiValueOverSignal = rsi.value > rsi.signal ? 100 : 0
//    // dmi
//    def dmi = analysis.dmis.first()
//    momentumScore.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
//    momentumScore.dmiAdx = dmi.adx > 20 && dmi.pdi > dmi.mdi ? 100 : 0
//    // obv
//    def obv = analysis.obvs.first()
//    momentumScore.obvValueOverSignal = obv.value > obv.signal ? 100 : 0
//    // co
//    def co = analysis.cos.first()
//    momentumScore.coValue = co.value > 0 ? 100 : 0
//    momentumScore.coSignal = co.signal > 0 ? 100 : 0

    // return
    return momentumScore
}

@ToString(includeNames = true)
class BuyScore implements Score {
    def macdValue
    def macdSignal
    def macdValueOverSignal
    def macdOscillator
    def rsiValue
    def rsiSignal
    def rsiValueOverSignal
    def dmiPdiOverMdi
    def dmiAdx
    def obvValueOverSignal
    def coValue
    def coSignal
}

static def getBuyScore(Analysis analysis) {
    def buyScore = new BuyScore()
    return buyScore
}


@ToString(includeNames = true)
class SellScore implements Score {
    def macdValue
    def macdSignal
    def macdValueOverSignal
    def macdOscillator
    def rsiValue
    def rsiSignal
    def rsiValueOverSignal
    def dmiPdiOverMdi
    def dmiAdx
    def obvValueOverSignal
    def coValue
    def coSignal
}

static def sellScore(Analysis analysis) {
    def sellScore = new SellScore()
    return sellScore
}

/**
 * return score average
 * @param score
 * @return score average
 */
static def getScoreAverage(Score score) {
    def average = score.properties.values()
            .findAll{it instanceof Number}
            .average()
    return BigDecimal.valueOf(average)
            .setScale(2, RoundingMode.HALF_UP)
}

//================================
// defines
//================================
def hold = null
def assetId = assetIndicator.getAssetId()
def assetName = "${assetIndicator.getAssetName()}(${assetId})"

// fast
List<Ohlcv> fastOhlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def fastPrices = fastOhlcvs.take(20).collect{it.closePrice}
def fastPriceZScore = tool.zScore(fastPrices)

// slow
List<Ohlcv> slowOhlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 10)
def slowPrices = slowOhlcvs.take(20).collect{it.closePrice}
def slowPriceZScore = tool.zScore(slowPrices)
def slowSma = tool.sma(slowPrices)
def slowSmaPctChange = tool.pctChange(slowPrices)

// logging
log.info("[{}] priceZScore: {}", assetName, fastPriceZScore)
log.info("[{}] longPriceZScore: {}", assetName, fastPriceZScore)

if(slowSmaPctChange > 0) {
    // buy
    if (slowPriceZScore > 0.0) {
        if (fastPriceZScore > 2.0) {
            hold = 1
        }
    }
    // sell
    if (slowPriceZScore < 0.0) {
        if (fastPriceZScore < -2.0) {
            hold = 0
        }
    }
}

//if (baseSmaPctChange < 0) {
//    hold = 0
//}



////=================================
//// asset momentum
////=================================
//Map<String, Analysis> assetAnalysises = [
//        minute: getAnalysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 10)),
//        hourly: getAnalysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 60)),
//        daily: getAnalysis(assetIndicator.getOhlcvs(Ohlcv.Type.DAILY, 1))
//]
//Map<String, MomentumScore> assetMomentumScores = [
//        minute: getMomentumScore(assetAnalysises.minute),
//        hourly: getMomentumScore(assetAnalysises.hourly),
//        daily: getMomentumScore(assetAnalysises.daily)
//]
//def assetMomentumScoreAverage = assetMomentumScores
//        .collect{getScoreAverage(it.value)}
//        .average()
//
////=================================
//// indice momentum
////=================================
//Map<String, Analysis> indiceAnalysises = [:]
//Map<String, MomentumScore> indiceMomentumScores = [:]
//
//
////=================================
//// default buy and sell
////=================================
//def momentumScoreAverage = [assetMomentumScoreAverage].average()
//
//// buy
//if (momentumScoreAverage > 70) {
//    hold = 1
//}
//
//// sell
//if (momentumScoreAverage < 50) {
//    hold = 0
//}

//==================================
// return
//==================================
return hold

