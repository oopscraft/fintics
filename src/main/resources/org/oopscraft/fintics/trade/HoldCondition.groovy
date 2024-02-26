import groovy.transform.ToString
import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.*

import java.math.RoundingMode

@ToString(includeNames = true)
class Analysis {
    List<Ohlcv> ohlcvs
    List<Ema> shortEmas
    List<Ema> longEmas
    List<Macd> macds
    List<Rsi> rsis
    List<Dmi> dmis
    List<Obv> obvs
    List<Co> cos
}

def getAnalysis(List<Ohlcv> ohlcvs) {
    def analysis = new Analysis()
    analysis.ohlcvs = ohlcvs
    analysis.shortEmas = tool.calculate(ohlcvs, EmaContext.of(5))
    analysis.longEmas = tool.calculate(ohlcvs, EmaContext.of(20))
    analysis.macds = tool.calculate(ohlcvs, MacdContext.DEFAULT)
    analysis.rsis = tool.calculate(ohlcvs, RsiContext.DEFAULT)
    analysis.dmis = tool.calculate(ohlcvs, DmiContext.DEFAULT)
    analysis.obvs = tool.calculate(ohlcvs, ObvContext.DEFAULT)
    analysis.cos = tool.calculate(ohlcvs, CoContext.DEFAULT)
    return analysis
}

@ToString(includeNames = true)
class MomentumScore {
    def emaValueShortOverLong
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

static def getMomentumScore(Analysis analysis) {
    def momentumScore = new MomentumScore()
    // ema
    def shortEma = analysis.shortEmas.first()
    def longEma = analysis.longEmas.first()
    momentumScore.emaValueShortOverLong = shortEma.value > longEma.value ? 100 : 0
    // macd
    def macd = analysis.macds.first()
    momentumScore.macdValue = macd.value > 0 ? 100 : 0
    momentumScore.macdSignal = macd.value > 0 ? 100 : 0
    momentumScore.macdOscillator = macd.oscillator > 0 ? 100 : 0
    momentumScore.macdValueOverSignal = macd.value > macd.oscillator ? 100 : 0
    // rsi
    def rsi = analysis.rsis.first()
    momentumScore.rsiValue = rsi.value > 50 ? 100 : 0
    momentumScore.rsiSignal = rsi.signal > 50 ? 100 : 0
    momentumScore.rsiValueOverSignal = rsi.value > rsi.signal ? 100 : 0
    // dmi
    def dmi = analysis.dmis.first()
    momentumScore.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
    momentumScore.dmiAdx = dmi.adx > 20 && dmi.pdi > dmi.mdi ? 100 : 0
    // obv
    def obv = analysis.obvs.first()
    momentumScore.obvValueOverSignal = obv.value > obv.signal ? 100 : 0
    // co
    def co = analysis.cos.first()
    momentumScore.coValue = co.value > 0 ? 100 : 0
    momentumScore.coSignal = co.signal > 0 ? 100 : 0
    // return
    return momentumScore
}

static def getScoreAverage(score) {
    def average = score.properties.values()
            .findAll{it instanceof Number}
            .average()
    return BigDecimal.valueOf(average)
            .setScale(2, RoundingMode.HALF_UP)
}



// defines
def hold = null
def assetId = assetIndicator.getAssetId()
def assetName = "${assetIndicator.getAssetName()}(${assetId})"


// ohlcv
List<Ohlcv> ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def prices = ohlcvs.collect{it.closePrice}
def price = prices.first()
def priceSma = tool.sma(prices.take(20))
def priceZScore = tool.zScore(prices.take(20))


if (price > priceSma) {
    if(priceZScore < 0.5) {
        hold = 1
    }
    if(priceZScore > 1.5) {
        hold = 0
    }
}

// fallback
if (price < priceSma) {
    hold = 0
}

// return
return hold

////====================================
//// momentum
////====================================
//// analysis
//def analysises = [
//        minute: getAnalysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 10)),
////        hourly : getAnalysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 60)),
////        daily : getAnalysis(assetIndicator.getOhlcvs(Ohlcv.Type.DAILY, 1))
//]
//// momentum score
//def momentumScores = [
//        minute: getMomentumScore(analysises.minute),
////        hourly: getMomentumScore(analysises.hourly),
////        daily: getMomentumScore(analysises.daily)
//]
//def momentumScoreAverage = momentumScores.collect{it.value.getAverage()}.average()
//momentumScores.each{key, value ->
//    log.info("[{}] momentumScores.{}: {}", assetName, key, value)
//}
//log.info("[{}] momentumScoreAverage: {}", assetName, momentumScoreAverage)
//
//// rsi
//def rsis = tool.calculate(ohlcvs, RsiContext.DEFAULT)
//def rsi = rsis.first()
//def rsiValue = rsi.value
//def rsiSignal = rsi.signal
//log.info("======== rsiValue: {}", rsiValue)
//log.info("======== rsiSignal: {}", rsiSignal)

//if(momentumScoreAverage > 50) {
    if (priceZScore > 2.0) {
        if (buyScoreAverage > 70) {
            hold = 1
        }
    }
    if (priceZScore < -2.0) {
        if (sellScoreAverage > 70) {
            hold = 0
        }
    }
//}

// sell
//if(balanceAsset != null) {
//    if (momentumScoreAverage < 50) {
//        hold = 0
//    }
//}

// return
return hold


////===================================
//// phase-1
////===================================
//if (dateTime.toLocalTime().isAfter(LocalTime.of(9,10))) {
//    // default buy
//    if (priceZScore > 1.5) {
//        if (momentumScoreAverage > 70) {
//            hold = 1
//        }
//    }
//    // default sell
//    if (priceZScore < -1.5) {
//        if (momentumScoreAverage < 50) {
//            hold = 0
//        }
//    }
//}
//
////====================================
//// phase-2
////====================================
//if (dateTime.toLocalTime().isAfter(LocalTime.of(11,0))) {
//    if (hold == 1) {
//        hold = null
//    }
//}
//
////====================================
//// phase-3
////====================================
//if (dateTime.toLocalTime().isAfter(LocalTime.of(15,10))) {
//    // buy
//    if (longMomentumScoreAverage > 70) {
//        hold = 1
//    }
//    // sell
//    if (longMomentumScoreAverage < 50) {
//        hold = 0
//    }
//}
//
////====================================
//// default fallback
////====================================
//if(longMomentumScoreAverage < 50) {
//     hold = 0
//}
//
//// return
//return hold

