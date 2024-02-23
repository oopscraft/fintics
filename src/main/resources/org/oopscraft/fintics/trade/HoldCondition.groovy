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


def getAnalysis(Indicator indicator, Ohlcv.Type ohlcvType, int ohlcvPeriod) {
    def analysis = new Analysis()

    // ema
    def shortEmas = indicator.calculate(EmaContext.of(10), ohlcvType, ohlcvPeriod)
    def longEmas = indicator.calculate(EmaContext.of(30), ohlcvType, ohlcvPeriod)
    def shortEma = shortEmas.first()
    def longEma = longEmas.first()
    analysis.emaValueShortOverLong = (shortEma.value > longEma.value ? 100 : 0)

    // macd
    def macds = indicator.calculate(MacdContext.DEFAULT, ohlcvType, ohlcvPeriod)
    def macd = macds.first()
    analysis.macdValue = (macd.value > 0 ? 100 : 0)
    analysis.macdValueOverSignal = (macd.value > macd.signal ? 100 : 0)
    analysis.macdOscillator = (macd.oscillator > 0 ? 100 : 0)

    // rsi
    def rsis = indicator.calculate(RsiContext.DEFAULT, ohlcvType, ohlcvPeriod)
    def rsi = rsis.first()
    analysis.rsiValue = (rsi.value > 0 ? 100 : 0)
    analysis.rsiValueOverSignal = (rsi.value > rsi.signal ? 100 : 0)

    // dmi
    def dmis = indicator.calculate(DmiContext.DEFAULT, ohlcvType, ohlcvPeriod)
    def dmi = dmis.first()
    analysis.dmiPdiOverMdi = (dmi.pdi > dmi.mdi ? 100 : 0)
    analysis.dmiAdx = (dmi.adx > 20 && dmi.pdi > dmi.mdi ? 100 : 0)

    // obv
    def obvs = indicator.calculate(ObvContext.DEFAULT, ohlcvType, ohlcvPeriod)
    def obv = obvs.first()
    analysis.obvValueOverSignal = (obv.value > obv.signal ? 100 : 0)

    // co
    def cos = indicator.calculate(CoContext.DEFAULT, ohlcvType, ohlcvPeriod)
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
def priceEmas = tool.emas(prices.take(20))
def pricePctChange = tool.pctChange(priceEmas.take(20))
log.info("[{}] priceZScore: {}", assetName, priceZScore)
log.info("[{}] pricePctChange: {}", assetName, pricePctChange)
log.info("[{}] priceEmas: {}", assetName, priceEmas)

// volume
def volumes = ohlcvs.collect{it.volume}
def volumeZScore = tool.zScore(volumes.take(20))
def volumeEmas = tool.emas(volumes.take(20))
def volumePctChange = tool.pctChange(prices.take(20))
log.info("[{}] volumeZScore: {}", assetName, volumeZScore)
log.info("[{}] volumePctChange: {}", assetName, volumePctChange)
log.info("[{}] volumeEmas: {}", assetName, volumes)

// analysis
//def analysis = getAnalysis(assetIndicator, Ohlcv.Type.MINUTE, 1)
//def analysisScore = analysis.getScore()
//log.info("[{}] analysis: {}", assetName, analysis)
//log.info("[{}] analysisScore(): {}", assetName, analysisScore)

// buy
if(priceZScore > 2.0 && pricePctChange > 0.0) {
//    if (analysisScore > 75) {
        hold = 1
//    }
}

// sell
if(priceZScore < -2.0 && pricePctChange < 0.0) {
//    if (analysisScore < 50) {
        hold = 0
//    }
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
