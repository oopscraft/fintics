import org.jetbrains.annotations.NotNull
import org.oopscraft.fintics.indicator.*
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.model.TradeAsset
import org.oopscraft.fintics.model.StrategyResult
import org.oopscraft.fintics.model.StrategyResult.Action
import org.oopscraft.fintics.trade.Tools

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
    BigDecimal getCurrentClose()
    BigDecimal getAverageClose()
    BigDecimal applyAveragePosition(BigDecimal position)
    Scorable getTrendScore()
    Scorable getMomentumScore()
    Scorable getDirectionScore()
    Scorable getVolatilityScore()
    Scorable getOversoldScore()
    Scorable getOverboughtScore()
}

class Analysis implements Analyzable {
    List<Ohlcv> ohlcvs
    Ohlcv ohlcv
    List<Sma> sma20s
    Sma sma20
    List<Sma> sma50s
    Sma sma50
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

    Analysis(TradeAsset profile, Ohlcv.Type type, int period) {
        this.ohlcvs = profile.getOhlcvs(type, period)
        this.ohlcv = this.ohlcvs.first()
        this.sma20s = Tools.indicators(ohlcvs, SmaContext.of(20))
        this.sma20 = sma20s.first()
        this.sma50s = Tools.indicators(ohlcvs, SmaContext.of(50))
        this.sma50 = sma50s.first()
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
    BigDecimal getCurrentClose() {
        return ohlcv.close
    }

    @Override
    BigDecimal getAverageClose() {
        return Tools.mean(ohlcvs.take(20).collect{it.close})
    }

    @Override
    BigDecimal applyAveragePosition(BigDecimal position) {
        def averagePrice = this.getAverageClose()
        def currentPrice = this.getCurrentClose()
        def averageWeight = averagePrice/currentPrice as BigDecimal
        def averagePosition = ((position * averageWeight) as BigDecimal)
                .setScale(position.scale(), RoundingMode.HALF_UP)
        return averagePosition
    }

    @Override
    Scorable getTrendScore() {
        def score = new Score()
        // sma20
        score.sma20Value = ohlcv.close > sma20.value ? 100 : 0
        // sma50
        score.sma50Value = ohlcv.close > sma50.value ? 100 : 0
        // macd
        score.macdValue = macd.value > 0 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getMomentumScore() {
        def score = new Score()
        // ema
        score.emaPriceOverValue = ohlcv.close > ema.value ? 100 : 0
        // macd
        score.macdValue = macd.value > 0 ? 100 : 0
        // bollinger band
        score.bollingerBandPriceOverMiddle = ohlcv.close > bollingerBand.middle ? 100 : 0
        // rsi
        score.rsiValue = rsi.value > 50 ? 100 : 0
        // cci
        score.cciValue = cci.value > 0 ? 100 : 0
        // dmi
        score.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValue = chaikinOscillator.value > 0 ? 100 : 0
        // obv
        def obvValuePctChange = Tools.pctChange(obvs.take(20).collect{it.value})
        score.obvValuePctChange = obvValuePctChange > 0.0 ? 100 : 0
        // stochastic slow
        score.stochasticSlowK = stochasticSlow.slowK > 50 ? 100 : 0
        // williams r
        score.williamsRValue = williamsR.value > -50 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getDirectionScore() {
        def score = new Score()
        // macd
        score.macdValueOversignal = macd.value > macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
        // rsi
        score.rsiValueOverSignal = rsi.value > rsi.signal ? 100 : 0
        // cci
        score.cciValueOverSignal = cci.value > cci.signal ? 100 : 0
        // obv
        score.obvValueOverSignal = obv.value > obv.signal ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValueOverSignal = chaikinOscillator.value > chaikinOscillator.signal ? 100 : 0
        // stochastic slow
        score.stochasticSlowKOverD = stochasticSlow.slowK > stochasticSlow.slowD ? 100 : 0
        // williams r
        score.williamsRValueOverSignal = williamsR.value > williamsR.signal ? 100 : 0
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
        score.rsi = rsis.take(3).any{it.value <= 30} ? 100 : 0
        // stochastic slow
        score.stochasticSlow = stochasticSlows.take(3).any{it.slowK <= 20} ? 100 : 0
        // williams r
        score.williamsR = williamsRs.take(3).any{it.value <= -80} ? 100 : 0
        // cci
        score.cci = williamsRs.take(3).any{it.value <= -100} ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOverboughtScore() {
        def score = new Score()
        // rsi
        score.rsi = rsis.take(3).any{it.value >= 70} ? 100 : 0
        // stochastic slow
        score.stochasticSlow = stochasticSlows.take(3).any{it.slowK >= 80} ? 100 : 0
        // williams r
        score.williamsR = williamsRs.take(3).any{it.value >= -20} ? 100 : 0
        // cci
        score.cci = ccis.take(3).collect{it.value >= 100} ? 100 : 0
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


    BigDecimal getCurrentClose() {
        return this.values().collect{it.getCurrentClose()}.average() as Number
    }

    @Override
    BigDecimal getAverageClose() {
        return this.values().collect{it.getAverageClose()}.average() as Number
    }

    @Override
    BigDecimal applyAveragePosition(BigDecimal position) {
        return this.values().collect{it.getAverageClose()}.average() as Number
    }

    @Override
    Scorable getTrendScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getTrendScore())}
        return scoreGroup
    }

    @Override
    Scorable getMomentumScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getMomentumScore())}
        return scoreGroup
    }

    @Override
    Scorable getDirectionScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getDirectionScore())}
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
def tideAnalysis = new Analysis(tradeAsset, tideOhlcvType, tideOhlcvPeriod)
def waveAnalysis = new Analysis(tradeAsset, waveOhlcvType, waveOhlcvPeriod)
def rippleAnalysis = new Analysis(tradeAsset, rippleOhlcvType, rippleOhlcvPeriod)
log.info("tide.momentum: {}", tideAnalysis.getMomentumScore())
log.info("wave.momentum: {}", waveAnalysis.getMomentumScore())
log.info("wave.volatility: {}", waveAnalysis.getVolatilityScore())
log.info("wave.oversold: {}", waveAnalysis.getOversoldScore())
log.info("wave.overbought: {}", waveAnalysis.getOverboughtScore())
log.info("ripple.momentum: {}", rippleAnalysis.getMomentumScore())

// position
def positionScore = tideAnalysis.getTrendScore().getAverage()
def marginPosition = 1.0 - basePosition
def positionPerScore = (marginPosition/100)
def position = (basePosition + (positionPerScore * positionScore)) as BigDecimal

// tide,wave,ripple average position
def tideAveragePosition = tideAnalysis.applyAveragePosition(position)
def waveAveragePosition = waveAnalysis.applyAveragePosition(position)
def rippleAveragePosition = tideAnalysis.applyAveragePosition(position)
log.info("position: {}", position)
log.info("tideAveragePosition: {}", tideAveragePosition)
log.info("waveAveragePosition: {}", waveAveragePosition)
log.info("rippleAveragePosition: {}", rippleAveragePosition)

// message
def message = """
position:${position} (tide:${tideAveragePosition}|wave:${waveAveragePosition}|ripple:${rippleAveragePosition})
tide.tre:${tideAnalysis.getTrendScore()}
tide.mom:${tideAnalysis.getMomentumScore().getAverage()}|vol:${tideAnalysis.getVolatilityScore().getAverage()}|osd:${tideAnalysis.getOversoldScore().getAverage()}|obt:${tideAnalysis.getOverboughtScore().getAverage()}
+ rsi:${tideAnalysis.rsi.value}|sto:${tideAnalysis.stochasticSlow.slowK}|cci:${tideAnalysis.cci.value}|wil:${tideAnalysis.williamsR.value}
wave.mom:${waveAnalysis.getMomentumScore().getAverage()}|vol:${waveAnalysis.getVolatilityScore().getAverage()}|osd:${waveAnalysis.getOversoldScore().getAverage()}|obt:${waveAnalysis.getOverboughtScore().getAverage()}
+ rsi:${waveAnalysis.rsi.value}|sto:${waveAnalysis.stochasticSlow.slowK}|cci:${waveAnalysis.cci.value}|wil:${waveAnalysis.williamsR.value}
ripple.mom:${rippleAnalysis.getMomentumScore().getAverage()}
"""
tradeAsset.setMessage(message)

// wave volatility 상태인 경우
if (waveAnalysis.getVolatilityScore() > 50) {
    // 중기 과매도 상태
    if (waveAnalysis.getOversoldScore() > 50) {
        // 단기 상승 모멘텀
        if (rippleAnalysis.getMomentumScore() > 80) {
            // 매수 포지션
            strategyResult = StrategyResult.of(Action.BUY, waveAveragePosition, "wave oversold buy: " + message)
        }
    }
    // 중기 과매수 상태
    if (waveAnalysis.getOverboughtScore() > 50) {
        // 단기 하락 모멘텀
        if (rippleAnalysis.getMomentumScore() < 20) {
            // 매도 포지션
            strategyResult = StrategyResult.of(Action.SELL, waveAveragePosition, "wave overbought sell: " + message)
        }
    }
}

// tide volatility 생태인 경우
if (tideAnalysis.getVolatilityScore() > 50) {
    // 장기 과매도 상태
    if (tideAnalysis.getOversoldScore() > 50) {
        // 중기 상승 모멘텀
        if (waveAnalysis.getMomentumScore() > 80) {
            // 매수 포지션
            strategyResult = StrategyResult.of(Action.BUY, tideAveragePosition, "tide oversold buy: " + message)
        }
    }
    // 장기 과매수 상태
    if (tideAnalysis.getOverboughtScore() > 50) {
        // 중기 하락 모멘텀
        if (waveAnalysis.getMomentumScore() < 20) {
            // 매도 포지션
            strategyResult = StrategyResult.of(Action.SELL, tideAveragePosition, "tide overbought sold: " + message)
        }
    }
}

// return
return strategyResult
