import groovy.transform.ToString
import org.hibernate.annotations.common.reflection.XMember
import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.BalanceAsset
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.trade.Tool

import java.time.LocalTime

//==========================
// defines
//==========================
def hold = null
def assetId = assetIndicator.getAssetId()
def assetName = "${assetIndicator.getAssetName()}(${assetId})"
List<Ohlcv> minuteOhlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1)
List<Ohlcv> dailyOhlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.DAILY, 1)
def prices = minuteOhlcvs.collect{it.closePrice}
def zScores = Tool.zScores(prices.take(20))

// bollinger band
List<BollingerBand> bollingerBands = Tool.calculate(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 5), BollingerBandContext.DEFAULT)
def middles = bollingerBands.collect{it.middle}
def uppers = bollingerBands.collect{it.upper}
def lowers = bollingerBands.collect{it.lower}


if (zScores.first() > 2.0) {
    hold = 1
}

if (zScores.first() < -2.0) {
    hold = 0
}


return hold



//// analysis
//def fastAnalysis = new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1))
//def slowAnalysis = new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 5))
//def hourlyAnalysis = new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 60))
//def dailyAnalysis = new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.DAILY, 1))
//
//// range of price change
//def minuteOhlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1)
//def dailyOhlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.DAILY, 1)
//def price = minuteOhlcvs.first().closePrice
//def pricePctChange = Tool.pctChange([
//        minuteOhlcvs.first().closePrice,
//        dailyOhlcvs.first().openPrice
//])
//def highPricePctChangeAverage = dailyOhlcvs.take(14)
//        .collect{(it.highPrice-it.openPrice)/it.closePrice*100}
//        .average()
//def lowPricePctChangeAverage = dailyOhlcvs.take(14)
//        .collect{(it.lowPrice-it.openPrice)/it.closePrice*100}
//        .average()
//List<Ohlcv> todayOhlcvs = minuteOhlcvs.findAll{it.dateTime.toLocalDate().equals(dateTime.toLocalDate())}
//def todayHighPrice = todayOhlcvs.collect{it.closePrice}.max()
//def todayLowPrice = todayOhlcvs.collect{it.closePrice}.min()
//def todayMiddlePrice = (todayHighPrice + todayLowPrice)/2
//
//log.info("[{}] pricePctChange: {}", assetName, pricePctChange)
//log.info("[{}] highPricePctChangeAverage: {}", assetName, highPricePctChangeAverage)
//log.info("[{}] lowPricePctChangeAverage: {}", assetName, lowPricePctChangeAverage)
//
////==============================================
//// 단기 상승 시작 시
////==============================================
//if (fastAnalysis.getMomentumScore().getAverage() > 70) {
//    // 평균 하락 폭 보다 더 하락한 경우 매수
//    if (pricePctChange < lowPricePctChangeAverage) {
//        hold = 1
//    }
//    // 중기 과매도 구간 이면 매수
//    if (slowAnalysis.getOversoldScore().getAverage() > 50) {
//        hold = 1
//    }
//}
//
////===============================================
//// 단기 하락 시작 시
////===============================================
//if (fastAnalysis.getMomentumScore().getAverage() < 30) {
//    // 평균 상승 폭 더 상승한 경우 경우 매도
//    if (pricePctChange > highPricePctChangeAverage) {
//        hold = 0
//    }
//    // 중기 과매수 구간 이면 매도
//    if (slowAnalysis.getOverboughtScore().getAverage() > 50) {
//        hold = 0
//    }
//}
//
////==============================================
//// fallback - daily momentum 미달인 경우 모두 매도
////==============================================
//def marketMomentumScoreAverage = [
//        hourlyAnalysis.getMomentumScore().getAverage(),
//        dailyAnalysis.getMomentumScore().getAverage()
//].average()
//if (marketMomentumScoreAverage < 50) {
//    log.info("[{}] fallback - daily momentum is under 50", assetName)
//    hold = 0
//}
//
////==============================================
//// return
////==============================================
//return hold
//
