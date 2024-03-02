import groovy.transform.ToString
import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.BalanceAsset
import org.oopscraft.fintics.model.Ohlcv

import java.time.LocalTime

// defines
def hold = null
def assetId = assetIndicator.getAssetId()
def assetName = "${assetIndicator.getAssetName()}(${assetId})"

// ohlcv
List<Ohlcv> ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def prices = ohlcvs.collect{it.closePrice}
def price = prices.first()

// zScore
def priceZScore = tool.zScore(prices.take(20))
log.info("priceZScore: {}", priceZScore)

// filter (for performance)
if (priceZScore.abs() < 2) {
    return null
}

// ma
def priceMas = tool.calculate(ohlcvs, SmaContext.of(60))
def priceMaValues = priceMas.take(60).collect{it.value}
def priceMaValue = priceMaValues.first()

// rsi
def rsis = tool.calculate(ohlcvs, RsiContext.DEFAULT)
def rsi = rsis.first()

// buy
if (priceZScore > 2.0) {
    def goldenCrossed = tool.isCross(prices.take(5), priceMaValues.take(5))
    if (goldenCrossed) {
        hold = 1
    }
}

// sell
if (priceZScore < -2.0) {
    def deadCrossed = tool.isCross(prices.take(5), priceMaValues.take(5))
    if (deadCrossed) {
        hold = 0
    }
}

// last
if (dateTime.toLocalTime().isAfter(LocalTime.of(15,10))) {
//    if (price > priceMa.value) {
//        hold = 0
//    }
//    if (price < priceMa.value) {
//        hold = 1
//    }
}


//if (price > priceMa.value) {
//    if (priceZScore < 0.0) {
//        hold = 1
//    }
//    if (priceZScore > 2.0) {
//        hold = 0
//    }
//}
//
//if (price < priceMa.value) {
//    if (priceZScore < -2.0) {
//        hold = 1
//    }
//    if (priceZScore > 0.0) {
//        hold = 0
//    }
//}


//
//if (priceZScore > 2.0) {
//    if (price > priceMa.value) {
//        hold = 1
//    }
//}
//if (priceZScore < -2.0) {
//    if (price < priceMa.value) {
//        hold = 0
//    }
//}


// momentum
//if (scoreAverage > 80) {
//    hold = 1
//}
//if (scoreAverage < 20) {
//    hold = 0
//}

// default fallback
//def marketAverage = getScoreAverage(assetIndicator.getOhlcvs(Ohlcv.Type.DAILY,1))
//if (marketAverage < 50) {
//    hold = 0
//}



//// fast ma
//List<Sma> fastMas = tool.calculate(ohlcvs, SmaContext.of(40)).take(40)
//def fastMaValues = fastMas.collect{it.value}
//def fastMaValue = fastMaValues.first()
//
//// slow ma
//List<Sma> slowMas = tool.calculate(ohlcvs, SmaContext.of(60)).take(60)
//def slowMaValues = slowMas.collect{it.value}
//def slowMaValue = slowMaValues.first()
//
//// buy
//if (fastZScore > 2.0) {
//    if (price > slowMaValue) {
//        hold = 1
//    }
//}
//
//// sell
//if (fastZScore < -2.0) {
//    if (price < fastMaValue) {
//        hold = 0
//    }
//}
//
////BalanceAsset balanceAsset = balance.getBalanceAsset(assetId).orElse(null)
////if (balanceAsset != null) {
////    if(balanceAsset.getProfitPercentage() > 0.5) {
////        hold = 0
////    }
////}
//
//
//// fallback
////if ( [slowScoreAverage, longScoreAverage].average() > 70) {
////    hold = 1
////}
////if ( [longScoreAverage].average() < 50) {
////    hold = 0
////}
//
//

//
//// fallback
////if (slowMaValuePctChange < 0) {
////    hold = 0
////}

// return
return hold




