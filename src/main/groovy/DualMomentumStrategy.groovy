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
        return Double.compare(this.getAverage().doubleValue(), o.getAverage().doubleValue())
    }
    default int compareTo(@NotNull Number o) {
        return Double.compare(this.getAverage().doubleValue(), o.doubleValue())
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

interface Analyzable {
    BigDecimal getCurrentPrice()
    BigDecimal getAveragePrice()
    Scorable getMomentumScore()
    Scorable getVolatilityScore()
    Scorable getOversoldScore()
    Scorable getOverboughtScore()
}

class Analysis implements Analyzable {
    def period = 10
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
        this.emas = Tools.indicators(ohlcvs, EmaContext.DEFAULT)
        this.ema = emas.first()
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
    BigDecimal getCurrentPrice() {
        return ohlcv.closePrice
    }

    @Override
    BigDecimal getAveragePrice() {
        return ema.value
    }

    @Override
    Scorable getMomentumScore() {
        def score = new Score()
        // ema
        score.emaValePctChange = Tools.pctChange(emas.take(period).collect{it.value}) > 0.0 ? 100 : 0
        score.emaPriceOverValue = ohlcv.closePrice > ema.value ? 100 : 0
        // macd
        score.macdValue = macd.value > 0 ? 100 : 0
        score.macdValueOverSignal = macd.value > macd.signal ? 100 : 0
        // rsi
        score.rsiValue = rsi.value > 50 ? 100 : 0
        score.rsiValueOverSignal = rsi.value > rsi.signal || (rsi.value == 100 && rsi.signal == 100) ? 100 : 0
        // bollinger band
        score.bollingerBandPriceOverMiddle = ohlcv.closePrice > bollingerBand.middle ? 100 : 0
        // cci
        score.cciValue = cci.value > 0 ? 100 : 0
        score.cciValueOverSignal = cci.value > cci.signal ? 100 : 0
        // dmi
        score.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
        // obv
        score.obvValuePctChange = Tools.pctChange(obvs.take(period).collect{it.value}) > 0.0 ? 100 : 0
        score.obvValueOverSignal = obv.value > obv.signal ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValue = chaikinOscillator.value > 0 ? 100 : 0
        score.chaikinOscillatorValueOverSignal = chaikinOscillator.value > chaikinOscillator.signal ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getVolatilityScore() {
        def score = new Score()
        // dmi
        score.dmiAdx = dmi.adx >= 25 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOversoldScore() {
        def score = new Score()
        // rsi
        score.rsi = rsi.value <= 30 || rsi.signal <= 30 ? 100 : 0
        // stochastic slow
        score.stochasticSlow = stochasticSlow.slowK <= 20 || stochasticSlow.slowD <= 20 ? 100 : 0
        // williams r
        score.williamsR = williamsR.value <= -80 || williamsR.signal <= -80 ? 100 : 0
        // cci
        score.cci = cci.value <= -100 || cci.signal <= -100 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOverboughtScore() {
        def score = new Score()
        // rsi
        score.rsi = rsi.value >= 70 || rsi.signal >= 70 ? 100 : 0
        // stochastic slow
        score.stochasticSlow = stochasticSlow.slowK >= 80 || stochasticSlow.slowD >= 80 ? 100 : 0
        // williams r
        score.williamsR = williamsR.value >= -20 || williamsR.signal >= -20 ? 100 : 0
        // cci
        score.cci = cci.value >= 100 || cci.signal >= 100 ? 100 : 0
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
    BigDecimal getCurrentPrice() {
        return this.values().average() as BigDecimal
    }

    @Override
    BigDecimal getAveragePrice() {
        return this.values().average() as BigDecimal
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

// config
log.info("variables: {}", variables)
def tideOhlcvType = variables['tideOhlcvType'] as Ohlcv.Type
def tideOhlcvPeriod = variables['tideOhlcvPeriod'] as Integer
def waveOhlcvType = variables['waveOhlcvType'] as Ohlcv.Type
def waveOhlcvPeriod = variables['waveOhlcvPeriod'] as Integer
def rippleOhlcvType = variables['rippleOhlcvType'] as Ohlcv.Type
def rippleOhlcvPeriod = variables['rippleOhlcvPeriod'] as Integer
def basePosition = variables['basePosition'] as BigDecimal

// result
StrategyResult strategyResult = null

// analysis
def tideAnalysis = new Analysis(assetProfile.getOhlcvs(tideOhlcvType, tideOhlcvPeriod))
def waveAnalysis = new Analysis(assetProfile.getOhlcvs(waveOhlcvType, waveOhlcvPeriod))
def rippleAnalysis = new Analysis(assetProfile.getOhlcvs(rippleOhlcvType, rippleOhlcvPeriod))
log.info("tide.momentum: {}", tideAnalysis.getMomentumScore())
log.info("wave.momentum: {}", waveAnalysis.getMomentumScore())
log.info("wave.volatility: {}", waveAnalysis.getVolatilityScore())
log.info("wave.oversold: {}", waveAnalysis.getOversoldScore())
log.info("wave.overbought: {}", waveAnalysis.getOverboughtScore())
log.info("ripple.momentum: {}", rippleAnalysis.getMomentumScore())

// position
def momentum = tideAnalysis.getMomentumScore().getAverage()
def marginPosition = 1.0 - basePosition
def positionPerMomentum = (marginPosition/100)
def position = ((basePosition + (positionPerMomentum * momentum)) as BigDecimal).setScale(2, RoundingMode.HALF_UP)
log.info("position(momentum): {} ({})", position, momentum)

// message
def message = """
position:${position}
tide.momentum: ${tideAnalysis.getMomentumScore().toString()}
wave.momentum|volatility|oversold|overbought:${waveAnalysis.getMomentumScore().getAverage()}|${waveAnalysis.getVolatilityScore().getAverage()}|${waveAnalysis.getOversoldScore().getAverage()}|${waveAnalysis.getOverboughtScore().getAverage()}
+ rsi:${waveAnalysis.rsi.value}|sto:${waveAnalysis.stochasticSlow.slowK}|cci:${waveAnalysis.cci.value}|wil:${waveAnalysis.williamsR.value}
ripple.momentum:${rippleAnalysis.getMomentumScore().getAverage()}
"""
messageTemplate.send(message)

// mean reversion strategy
if (waveAnalysis.getVolatilityScore() > 50) {
    if (waveAnalysis.getOversoldScore() > 50) {
        if (rippleAnalysis.getMomentumScore() > 75) {
            strategyResult = StrategyResult.of(Action.BUY, position, message)
        }
    }
    if (waveAnalysis.getOverboughtScore() > 50) {
        if (rippleAnalysis.getMomentumScore() < 25) {
            strategyResult = StrategyResult.of(Action.SELL, position, message)
        }
    }
}

// squeeze momentum strategy
if (waveAnalysis.getVolatilityScore() < 50) {
    if (waveAnalysis.getMomentumScore() > 75) {
        if (rippleAnalysis.getMomentumScore() > 75) {
            strategyResult = StrategyResult.of(Action.BUY, position, message)
        }
    }
    if (waveAnalysis.getMomentumScore() < 25) {
        if (rippleAnalysis.getMomentumScore() < 25) {
            strategyResult = StrategyResult.of(Action.SELL, position, message)
        }
    }
}

// return
return strategyResult
