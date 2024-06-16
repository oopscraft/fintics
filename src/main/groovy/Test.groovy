import org.jetbrains.annotations.NotNull
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.model.StrategyResult
import org.oopscraft.fintics.model.StrategyResult.Action
import org.oopscraft.fintics.trade.Tools
import org.oopscraft.fintics.indicator.*

import java.math.RoundingMode

interface Scorable extends Comparable<Scorable> {
    Number getAverage()
    @Override
    default int compareTo(@NotNull Scorable o) {
        return Double.compare(this.getAverage().doubleValue(), o.getAverage().doubleValue());
    }
    default int compareTo(@NotNull Number o) {
        return Double.compare(this.getAverage().doubleValue(), o.doubleValue())
    }
}

class ScoreGroup extends LinkedHashMap<String, Scorable> implements Scorable {
    @Override
    Number getAverage() {
        return this.values().collect{it.getAverage()}.average() as Number
    }
    @Override
    String toString() {
        return this.getAverage() + ' ' + super.toString()
    }
}

class Score extends LinkedHashMap<String, BigDecimal> implements Scorable {
    Number getAverage() {
        return this.values().empty ? 0 : this.values().average() as Number
    }
    @Override
    String toString() {
        return this.getAverage() + ' ' + super.toString()
    }
}

interface Analyzable {
    Scorable getDirectionScore()
    Scorable getMomentumScore()
    Scorable getVolatilityScore()
    Scorable getOversoldScore()
    Scorable getOverboughtScore()
}

class Analysis implements Analyzable {
    def period = 20
    List<Ohlcv> ohlcvs
    Ohlcv ohlcv
    List<Ema> emas
    Ema ema
    List<Macd> macds
    Macd macd
    List<BollingerBand> bollingerBands
    BollingerBand bollingerBand
    List<Dmi> dmis
    Dmi dmi
    List<Rsi> rsis
    Rsi rsi
    List<Cci> ccis
    Cci cci
    List<StochasticSlow> stochasticSlows
    StochasticSlow stochasticSlow
    List<WilliamsR> williamsRs
    WilliamsR williamsR
    List<Obv> obvs
    Obv obv
    List<ChaikinOscillator> chaikinOscillators
    ChaikinOscillator chaikinOscillator

    Analysis(List<Ohlcv> ohlcvs) {
        this.ohlcvs = ohlcvs
        this.ohlcv = this.ohlcvs.first()
        this.macds = Tools.indicators(ohlcvs, MacdContext.DEFAULT)
        this.macd = this.macds.first()
        this.bollingerBands = Tools.indicators(ohlcvs, BollingerBandContext.DEFAULT)
        this.bollingerBand = bollingerBands.first()
        this.dmis = Tools.indicators(ohlcvs, DmiContext.DEFAULT)
        this.dmi = this.dmis.first()
        this.rsis = Tools.indicators(ohlcvs, RsiContext.DEFAULT)
        this.rsi = rsis.first()
        this.ccis = Tools.indicators(ohlcvs, CciContext.DEFAULT)
        this.cci = ccis.first()
        this.stochasticSlows = Tools.indicators(ohlcvs, StochasticSlowContext.DEFAULT)
        this.stochasticSlow = stochasticSlows.first()
        this.williamsRs = Tools.indicators(ohlcvs, WilliamsRContext.DEFAULT)
        this.williamsR = williamsRs.first()
        this.obvs = Tools.indicators(ohlcvs, ObvContext.DEFAULT)
        this.obv = obvs.first()
        this.chaikinOscillators = Tools.indicators(ohlcvs, ChaikinOscillatorContext.DEFAULT)
        this.chaikinOscillator = chaikinOscillators.first()
    }

    @Override
    Scorable getDirectionScore() {
        def score = new Score()
        // macd
        score.macdValueOverSignal = macd.value > macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
        // rsi
        score.rsiValueOverSignal = rsi.value > rsi.signal ? 100 : 0
        // cci
        score.cciValueOverSignal = cci.value > cci.signal ? 100 : 0
        // obv
        score.obvValueOverSignal = obv.value > obv.signal ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValueOverSignal = chaikinOscillator.value > chaikinOscillator.signal ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getMomentumScore() {
        def score = new Score()
        // macd
        score.macdValue = macd.value > 0 ? 100 : 0
        // rsi
        score.rsiValue = rsi.value > 50 ? 100 : 0
        // bollinger band
        score.bollingerBandPriceOverMiddle = ohlcv.closePrice > bollingerBand.middle ? 100 : 0
        // cci
        score.cciValue = cci.value > 0 ? 100 : 0
        // dmi
        score.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
        // obv
        score.obvValuePctChange = Tools.pctChange(obvs.take(period).collect{it.value}) > 0.0 ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValue = chaikinOscillator.value > 0 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getVolatilityScore() {
        def score = new Score()
        // dmi
        def dmiAdxPctChange = Tools.pctChange(dmis.take(period).collect{it.adx})
        if (dmiAdxPctChange > 0.0 && dmi.adx > 25) {
            score.dmiAdxPctChange = 100
        } else {
            score.dmiAdxPctChange = 0
        }
        // return
        return score
    }

    @Override
    Scorable getOversoldScore() {
        def score = new Score()
        // rsi
        score.rsiValue = rsi.value < 30 || rsi.signal < 30 ? 100 : 0
        // stochastic slow
        score.stochasticSlowK = stochasticSlow.slowK < 20 || stochasticSlow.slowD < 20 ? 100 : 0
        // williams r
        score.williamsRValue = williamsR.value < -80 || williamsR.signal < -80 ? 100 : 0
        // cci
        score.cciValue = cci.value < -100 || rsi.signal < -100 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOverboughtScore() {
        def score = new Score()
        // rsi
        score.rsiValue = rsi.value > 70 || rsi.signal > 70 ? 100 : 0
        // stochastic slow
        score.stochasticSlowK = stochasticSlow.slowK > 80 || stochasticSlow.slowD > 80 ? 100 : 0
        // williams r
        score.williamsRValue = williamsR.value > -20 || williamsR.signal > -20 ? 100 : 0
        // cci
        score.cciValue = cci.value > 100 || cci.signal > 100 ? 100 : 0
        // return
        return score
    }

    @Override
    String toString() {
        return [
                momentumScore: "${this.getMomentumScore()}",
                volatilityScore: "${this.getVolatilityScore()}",
                oversoldScore: "${this.getOversoldScore()}",
                overboughtScore: "${this.getOverboughtScore()}"
        ].toString()
    }
}

class AnalysisGroup extends LinkedHashMap<String, Analyzable> implements Analyzable {

    @Override
    Scorable getDirectionScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getDirectionScore())}
        return scoreGroup
    }

    @Override
    Scorable getMomentumScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getMomentumScore())}
        return scoreGroup
    }

    @Override
    Scorable getVolatilityScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getVolatilityScore())}
        return scoreGroup
    }

    @Override
    Scorable getOversoldScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getOversoldScore())}
        return scoreGroup
    }

    @Override
    Scorable getOverboughtScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getOverboughtScore())}
        return scoreGroup
    }

}

//================================
// define
//================================
// config
log.info("variables: {}", variables)

// default
StrategyResult strategyResult = null


def rippleAnalysis = new Analysis(assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, 1))
def waveAnalysis = new Analysis(assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, 10))
def tideAnalysis = new Analysis(assetProfile.getOhlcvs(Ohlcv.Type.DAILY, 1))
log.info("ripple.direction: {}", rippleAnalysis.getDirectionScore().getAverage())
log.info("ripple.momentum: {}", rippleAnalysis.getMomentumScore().getAverage())
log.info("wave.direction: {}", waveAnalysis.getDirectionScore().getAverage())
log.info("wave.momentum: {}", waveAnalysis.getMomentumScore().getAverage())
log.info("tide.direction: {}", tideAnalysis.getDirectionScore().getAverage())
log.info("tide.momentum: {}", tideAnalysis.getMomentumScore().getAverage())

//================================
// trade
//================================
// 진입
if (balanceAsset == null) {
    strategyResult = StrategyResult.of(Action.BUY, 1.0, "buy")
}

// 과매도
if (waveAnalysis.getOversoldScore() > 50 && waveAnalysis.getVolatilityScore() > 50) {
    if (rippleAnalysis.getDirectionScore() > 75 && rippleAnalysis.getMomentumScore() > 50) {
        strategyResult = StrategyResult.of(Action.BUY, 1.0, "oversold buy")
    }
}
// 과매수
if (waveAnalysis.getOverboughtScore() > 50 && waveAnalysis.getVolatilityScore() > 50) {
    if (rippleAnalysis.getDirectionScore() < 25 && rippleAnalysis.getMomentumScore() < 50) {
        strategyResult = StrategyResult.of(Action.SELL, 1.0, "overbought sell")
    }
}

////=================================
//// fallback
////=================================
//def fallback = false
//// tide 하락 시
//if (tideAnalysis.getDirectionScore() < 25) {
//    fallback = true
//}
//if (tideAnalysis.getMomentumScore() < 50) {
//    fallback = true
//}
//if (fallback) {
//    strategyResult = StrategyResult.of(Action.SELL, 0.0, "fallback")
//}

//================================
// fallback
//================================
// tide 하락 추세
//if (tideAnalysis.getDirectionScore() < 75) {
//    strategyResult = StrategyResult.of(Action.SELL, 0.0, "entire sell")
//}







//// 중기 변동성 증가 시
//if (waveAnalysis.getVolatilityScore().getAverage() > 50) {
//    // 중기 과매도 시 매수
//    if (waveAnalysis.getOversoldScore().getAverage() > 50) {
//        if (rippleAnalysis.getDirectionScore().getAverage() > 75) {
//            strategyResult = StrategyResult.of(Action.BUY, 1.0, "buy")
//        }
//    }
//    // 과매수 시 매도
//    if (waveAnalysis.getOverboughtScore().getAverage() > 50) {
//        if (rippleAnalysis.getDirectionScore().getAverage() < 25) {
//            strategyResult = StrategyResult.of(Action.SELL, 0.5, "sell")
//        }
//    }
//}
//
//// 과매수 시 매도
//if (waveAnalysis.getVolatilityScore().getAverage() > 50) {
//
//}
//// 미 보유 시 (최초 진입 조건)
//if (balanceAsset == null) {
//    // 변동성 하락
//    if (waveAnalysis.getVolatilityScore().getAverage() < 50) {
//        // 단기 상승
//        if (rippleAnalysis.getMomentumScore().getAverage() > 75) {
//            strategyResult = StrategyResult.of(Action.BUY, 5.0, "buy")
//        }
//    }
//}
//
//// 모멘텀 하락 + 방향성 하락
//if (tideAnalysis.getMomentumScore().getAverage() < 50 || tideAnalysis.getDirectionScore().getAverage() < 25) {
//    // 청산
//    if (waveAnalysis.getOverboughtScore().getAverage() > 25) {
//        if (rippleAnalysis.getDirectionScore().getAverage() < 25) {
//            strategyResult = StrategyResult.of(Action.SELL, 0.0, "sell")
//        }
//    }
//}
//    if (slowAnalysis.getOversoldScore().getAverage() >= 50 && slowAnalysis.getVolatilityScore().getAverage() >= 50) {
//        if (fastAnalysis.getDirectionScore().getAverage() > 75) {
//            strategyResult = StrategyResult.of(Action.BUY, 1.0, "buy")
//        }
//    }
//    if (slowAnalysis.getOverboughtScore().getAverage() >= 50 && slowAnalysis.getVolatilityScore().getAverage() >= 50) {
//        if (fastAnalysis.getDirectionScore().getAverage() < 25) {
//            strategyResult = StrategyResult.of(Action.SELL, 0.0, "sell")
//        }
//    }
//    if (fastAnalysis.getMomentumScore().getAverage() > slowAnalysis.getMomentumScore().getAverage()) {
//        if (fastAnalysis.getOversoldScore().getAverage() >= 50) {
//            strategyResult = StrategyResult.of(Action.BUY, 1.0, "buy")
//        }
//    }
//
//    if (fastAnalysis.getMomentumScore().getAverage() < slowAnalysis.getMomentumScore().getAverage()) {
//        if (fastAnalysis.getOverboughtScore().getAverage() >= 50) {
//            strategyResult = StrategyResult.of(Action.SELL, 0.0, "sell")
//        }
//    }
//}
//if (tideAnalysis.getMomentumScore().getAverage() < 75 || tideAnalysis.getDirectionScore().getAverage() < 25) {
//    if (fastAnalysis.getOverboughtScore().getAverage() >= 25) {
//        strategyResult = StrategyResult.of(Action.SELL, 0.0, "sell")
//    }
//}
//
//
//
//
//if (slowAnalysis.getMomentumScore().getAverage() >= 90) {
////    if (fastAnalysis.getOversoldScore().getAverage() >= 25) {
//        strategyResult = StrategyResult.of(Action.BUY, 1.0, "buy")
////    }
//}
//if (slowAnalysis.getMomentumScore().getAverage() <= 10) {
////    if (fastAnalysis.getOverboughtScore().getAverage() >= 25) {
//        strategyResult = StrategyResult.of(Action.SELL, 0.0, "sell")
////    }
//}

//if (tideAnalysis.getMomentumScore().getAverage() < 50 && waveAnalysis.getMomentumScore().getAverage() < 50 && tideAnalysis.getMomentumScore().getAverage() < 50) {
//    strategyResult = StrategyResult.of(Action.SELL, 0.0, "sell")
//}

//================================
// return
//================================
return strategyResult
