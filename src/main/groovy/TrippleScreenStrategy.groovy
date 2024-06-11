import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.model.StrategyResult
import org.oopscraft.fintics.model.StrategyResult.Action
import org.oopscraft.fintics.trade.Tools
import org.oopscraft.fintics.indicator.*

import java.math.RoundingMode

interface Scorable {
    Number getAverage()
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
    List<Obv> obvs
    Obv obv
    List<ChaikinOscillator> chaikinOscillators
    ChaikinOscillator chaikinOscillator
    StochasticSlow stochasticSlow

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
        this.obvs = Tools.indicators(ohlcvs, ObvContext.DEFAULT)
        this.obv = obvs.first()
        this.chaikinOscillators = Tools.indicators(ohlcvs, ChaikinOscillatorContext.DEFAULT)
        this.chaikinOscillator = chaikinOscillators.first()
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
        score.dmiAdx = dmi.adx > 25 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOversoldScore() {
        def score = new Score()
        // rsi
        score.rsiValue = rsi.value < 30 || rsi.signal < 30 ? 100 : 0
        // cci
        score.cciValue = cci.value < -100 || rsi.signal < -100 ? 100 : 0
        // stochastic slow
        score.stochasticSlowK = stochasticSlow.slowK < 20 || stochasticSlow.slowD < 20 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOverboughtScore() {
        def score = new Score()
        // rsi
        score.rsiValue = rsi.value > 70 || rsi.signal > 70 ? 100 : 0
        // cci
        score.cciValue = cci.value > 100 || cci.signal > 100 ? 100 : 0
        // stochastic slow
        score.stochasticSlowK = stochasticSlow.slowK > 80 || stochasticSlow.slowD > 80 ? 100 : 0
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
def rippleOhlcvPeriod = variables['rippleOhlcvPeriod'] as Integer
def waveOhlcvPeriod = variables['waveOhlcvPeriod'] as Integer
def stopLoss = variables['stopLoss'] as BigDecimal

// default
StrategyResult strategyResult = null

// ripple
def rippleAnalysis = new Analysis(assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, rippleOhlcvPeriod))

// wave
def waveAnalysis = new Analysis(assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, waveOhlcvPeriod))

// tide
def tideAnalysis = new AnalysisGroup(
        hourly: new Analysis(assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, 60)),
        daily: new Analysis(assetProfile.getOhlcvs(Ohlcv.Type.DAILY, 1))
)

// 현재 수익률
def profitPercentage = balanceAsset?.getProfitPercentage() ?: 0.0

// logging
log.info("ripple.momentum: {}", rippleAnalysis.getMomentumScore())
log.info("wave.momentum: {}", waveAnalysis.getMomentumScore())
log.info("wave.oversold: {}", waveAnalysis.getOversoldScore())
log.info("wave.overbought: {}", waveAnalysis.getOverboughtScore())
log.info("wave.volatility: {}", waveAnalysis.getVolatilityScore())
log.info("tide.momentum: {}", tideAnalysis.getMomentumScore())
log.info("profitPercentage: {}", profitPercentage)

//================================
// trade
//================================
// 장기 상승 시
if (tideAnalysis.getMomentumScore().getAverage() > 50) {
    // 중기 과매도 시
    if (waveAnalysis.getOversoldScore().getAverage() > 50 && waveAnalysis.getVolatilityScore().getAverage() > 50) {
        // 단기 상승 시
        if (rippleAnalysis.getMomentumScore().getAverage() > 75) {
            // 매수 포지션
            strategyResult = StrategyResult.of(Action.BUY, 1.0, "wave.oversold: ${waveAnalysis.getOversoldScore()}")
        }
    }
    // 중기 과매수 시
    if (waveAnalysis.getOverboughtScore().getAverage() > 50 && waveAnalysis.getVolatilityScore().getAverage() > 50) {
        // 단기 하락 시
        if (rippleAnalysis.getMomentumScore().getAverage() < 75) {
            // 매도 포지션
            strategyResult = StrategyResult.of(Action.SELL, 0.5, "wave.overbought: ${waveAnalysis.getOverboughtScore()}")
        }
    }
}
// 장기 하락 시
if (tideAnalysis.getMomentumScore().getAverage() < 50) {
    // 중기 과매수 시
    if (waveAnalysis.getOverboughtScore().getAverage() > 50) {
        // 단기 하락 시
        if (rippleAnalysis.getMomentumScore().getAverage() < 25) {
            // 매도 포지션
            strategyResult = StrategyResult.of(Action.SELL, 0.0, "tide.momentum: ${tideAnalysis.getMomentumScore()}")
        }
    }
}
// fallback - 장기 하락 추세인 경우 모두 매도 포지션
if (tideAnalysis.getMomentumScore().getAverage() < 25) {
    if (waveAnalysis.getMomentumScore().getAverage() < 25) {
        if (rippleAnalysis.getMomentumScore().getAverage() < 25) {
            strategyResult = StrategyResult.of(Action.SELL, 0.0, "tide.momentum: ${tideAnalysis.getMomentumScore()}")
        }
    }
}
// fallback - 손실 제한 (stopLoss) 설정 시 이하로 하락 시 강제 매도
if (rippleAnalysis.getMomentumScore().getAverage() < 25) {
    if (stopLoss < 0.0) {
        if (profitPercentage < stopLoss) {
            strategyResult = StrategyResult.of(Action.SELL, 0.0, "stopLoss: ${profitPercentage}")
        }
    }
}

//================================
// return
//================================
return strategyResult
