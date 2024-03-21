import groovy.transform.ToString
import org.jetbrains.annotations.NotNull
import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.trade.Tool

import java.time.LocalTime

def hold = null
List<Ohlcv> ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 10)
def ohlcv = ohlcvs.first()
def priceZScore = Tool.zScore(ohlcvs.take(10).collect{it.closePrice})

// ema
List<Ema> emas = Tool.calculate(ohlcvs, EmaContext.of(10))
def ema = emas.first()
def emaValues = emas.take(20).collect{it.value}
def emaValuePctChange = Tool.pctChange(emaValues)

// dmis
List<Dmi> dmis = Tool.calculate(ohlcvs, DmiContext.DEFAULT)
def dmi = dmis.first()
def dmiAdxs = dmis.collect{it.adx}
def dmiPdis = dmis.collect{it.pdi}
def dmiMdis = dmis.collect{it.mdi}
def dmiAdxPctChange = Tool.pctChange(dmiAdxs.take(10))
def dmiPdiPctChange = Tool.pctChange(dmiPdis.take(10))
def dmiMdiPctChange = Tool.pctChange(dmiMdis.take(10))

// macd
List<Macd> macds = Tool.calculate(ohlcvs, MacdContext.DEFAULT)
def macd = macds.first()
def macdOscillators = macds.collect{it.oscillator}
def macdOscillatorPctChange = Tool.pctChange(macdOscillators.take(10))

// bollinger band
List<BollingerBand> bollingerBands = Tool.calculate(ohlcvs, BollingerBandContext.DEFAULT)
def bollingerBand = bollingerBands.first()

// rsi
List<Rsi> rsis = Tool.calculate(ohlcvs, RsiContext.DEFAULT)
def rsi = rsis.first()

// cci
List<Cci> ccis = Tool.calculate(ohlcvs, CciContext.of(20,10))
def cci = ccis.first()

// atr
List<Atr> atrs = Tool.calculate(ohlcvs, AtrContext.DEFAULT)
def atr = atrs.first()

// trailing stop
Ohlcv prevOhlcv = ohlcvs.get(1)
Atr prevAtr = atrs.get(1)
def trailingStop = ohlcv.closePrice < prevOhlcv.highPrice - (prevAtr.value * 2.0)
log.info("- trailing stop: {}", trailingStop)

// tide macd
List<Macd> tideMacds = Tool.calculate(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 60), MacdContext.DEFAULT)
def tideMacd = tideMacds.first()


// test
if (priceZScore.abs() < 1.0) {
    return null
}
log.info("====== {}", macdOscillatorPctChange)


if (dmi.adx > 25) {
    if (macd.oscillator > 0) {
        hold = 1
    }

}
if (macd.oscillator < 0) {
    hold = 0
}



if (trailingStop) {
    hold = 0
}

if (tideMacd.value < 0 || tideMacd.oscillator < 0) {
    hold = 0
}





//if (trailingStop) {
//    hold = 0
//}




//// test-1: 상승장(20%) 코스닥레버+인버: 4.7%(2.5~3.4), 코스닥레버: 13%(2.5~3.4)
//if (dmi.adx > 25) {
//    if (macd.oscillator > 0) {
//        hold = 1
//    }
//    if (macd.oscillator < 0) {
//        hold = 0
//    }
//}

//// 결과: 코스닥레버:10%(2.5~3.4), 코스피레버:9%(2.5~3.4)
//if (dmi.adx > 25 && bollingerBand.width > 0.01) {
//    if (macd.oscillator > 0) {
//        hold = 1
//    }
//    if (macd.oscillator < 0) {
//        hold = 0
//    }
//}

//// test-1:
//if (priceZScore >= 1.5) {
//    if (rsi.value < 30 && rsi.value > rsi.signal) {
//        hold = 1
//    }
//}
//if (priceZScore <= -1.5) {
//    if (rsi.value > 70 && rsi.value < rsi.signal) {
//        hold = 0
//    }
//}

//// test - 코스닥 레버: 15%
//if (priceZScore > 1.5) {
//    if (dmi.adx > 25) {
//        if (macd.value < 0 && macd.oscillator > 0) {
//            hold = 1
//        }
//        if (rsi.value < 30 && rsi.value > rsi.signal) {
//            hold = 1
//        }
//    }
//}
//if (priceZScore < -1.5) {
//    if (dmi.adx > 25) {
//        if (macd.value > 0 && macd.oscillator < 0) {
//            hold = 0
//        }
//        if (rsi.value > 70 && rsi.value < rsi.signal) {
//            hold = 0
//        }
//    }
//}


//if (tideMacd.oscillator > 0) {
//    if (priceZScore < -2.0) {
//        if (rsi.value >= 70 && rsi.value < rsi.signal) {
//            hold = 0
//        }
//    }
//    if (priceZScore > 2.0) {
//        if (rsi.value <= 30 && rsi.value > rsi.signal) {
//            hold = 1
//        }
//    }
//}
//
//if (tideMacd.oscillator < 0) {
//    hold = 0
//}




//List<Macd> slowMacds = Tool.calculate(assetIndicator.getOhlcvs(Ohlcv.Type.DAILY, 1), MacdContext.DEFAULT)
//def slowMacd = slowMacds.first()
//if (slowMacd.oscillator < 0) {
//    hold = 0
//}


//// test-2 : 4.5%(2.5-3.4)
//def dmiAdxPctChange = Tool.pctChange(dmiAdxs.take(5))
//def macdOscillatorPctChange = Tool.pctChange(macdOscillators.take(5))
//if (dmi.adx > 25) {
//    if (macd.oscillator > 0 && dmiAdxPctChange > 0) {
//        hold = 1
//    }
//    if (macd.oscillator < 0 && dmiAdxPctChange < 0) {
//        hold = 0
//    }
//}


//// test-2: fail
//List<Macd> macds = Tool.calculate(ohlcvs, MacdContext.of(24, 52, 18))
//def macd = macds.first()
//if (dmi.adx > 25) {
//    if (macd.oscillator > 0) {
//        hold = 1
//    }
//    if (macd.oscillator < 0) {
//        hold = 0
//    }
//}


// return
return hold

