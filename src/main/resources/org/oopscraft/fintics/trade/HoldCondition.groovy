import groovy.transform.ToString
import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.*

import java.math.RoundingMode
import java.time.LocalTime

@ToString(includeNames = true)
class Analysis {
    def emaValueShortOverLong
    def macdValue
    def macdValueOverSignal
    def macdOscillator
    def rsiValue
    def rsiValueOverSignal
    def dmiPdiOverMdi
    def dmiAdx
    def obvValueOverSignal
    def coValue
    def coValueOverSignal

    def getScore() {
        def average = this.properties.values()
                .findAll{it instanceof Number}
                .average()
        return BigDecimal.valueOf(average)
                .setScale(2, RoundingMode.HALF_UP)
    }
}


def getAnalysis(Ohlcv.Type ohlcvType, int ohlcvPeriod) {
    def analysis = new Analysis()
    def ohlcvs = assetIndicator.getOhlcvs(ohlcvType, ohlcvPeriod)

    // ema
    def shortEmas = tool.calculate(ohlcvs, EmaContext.of(10))
    def longEmas = tool.calculate(ohlcvs, EmaContext.of(30))
    def shortEma = shortEmas.first()
    def longEma = longEmas.first()
    analysis.emaValueShortOverLong = (shortEma.value > longEma.value ? 100 : 0)

    // macd
    def macds = tool.calculate(ohlcvs, MacdContext.DEFAULT)
    def macd = macds.first()
    analysis.macdValue = (macd.value > 0 ? 100 : 0)
    analysis.macdValueOverSignal = (macd.value > macd.signal ? 100 : 0)
    analysis.macdOscillator = (macd.oscillator > 0 ? 100 : 0)

    // rsi
    def rsis = tool.calculate(ohlcvs, RsiContext.DEFAULT)
    def rsi = rsis.first()
    analysis.rsiValue = (rsi.value > 0 ? 100 : 0)
    analysis.rsiValueOverSignal = (rsi.value > rsi.signal ? 100 : 0)

    // dmi
    def dmis = tool.calculate(ohlcvs, DmiContext.DEFAULT)
    def dmi = dmis.first()
    analysis.dmiPdiOverMdi = (dmi.pdi > dmi.mdi ? 100 : 0)
    analysis.dmiAdx = (dmi.adx > 20 && dmi.pdi > dmi.mdi ? 100 : 0)

    // obv
    def obvs = tool.calculate(ohlcvs, ObvContext.DEFAULT)
    def obv = obvs.first()
    analysis.obvValueOverSignal = (obv.value > obv.signal ? 100 : 0)

    // co
    def cos = tool.calculate(ohlcvs, CoContext.DEFAULT)
    def co = cos.first()
    analysis.coValue = (co.value > 0 ? 100 : 0)
    analysis.coValueOverSignal = (co.value > co.signal ? 100 : 0)

    // return
    return analysis
}

// defines
def hold = null
def assetId = assetIndicator.getAssetId()
def assetName = "${assetIndicator.getAssetName()}(${assetId})"

// ohlcv
def ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1).findAll{it.dateTime.toLocalDate().equals(dateTime.toLocalDate())}

// price
def prices = ohlcvs.collect{it.closePrice}
def priceZScore = tool.zScore(prices.take(20))
log.info("[{}] priceZScore: {}", assetName, priceZScore)

// analysis
//def analysis = getAnalysis(assetIndicator, Ohlcv.Type.MINUTE, 1)
//def analysisScore = analysis.getScore()
//log.info("[{}] analysis: {}", assetName, analysis)
//log.info("[{}] analysisScore(): {}", assetName, analysisScore)

// buy
if(priceZScore > 2.0) {
    def analysis = getAnalysis(Ohlcv.Type.MINUTE, 1)
    if (analysis.getScore() > 75) {
        hold = 1
    }
}

// sell
if(priceZScore < -2.0) {
    def analysis = getAnalysis(Ohlcv.Type.MINUTE, 1)
    if (analysis.getScore() < 50) {
        hold = 0
    }
}

// time range
if (dateTime.toLocalTime().isBefore(LocalTime.of(9,20))) {
    hold = null
}
//if (dateTime.toLocalTime().isAfter(LocalTime.of(11,0))) {
//    if (hold == 1) {
//        hold = null
//    }
//}
if (dateTime.toLocalTime().isAfter(LocalTime.of(15,10))) {
    hold = 0
}

// return
return hold
