import org.jetbrains.annotations.NotNull
import org.oopscraft.fintics.indicator.*
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.model.TradeAsset
import org.oopscraft.fintics.trade.strategy.StrategyResult
import org.oopscraft.fintics.trade.strategy.StrategyResult.Action
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
    Scorable getTrailingStopScore()
}

class Analysis implements Analyzable {
    List<Ohlcv> ohlcvs
    Ohlcv ohlcv
    List<Sma> sma5s
    Sma sma5
    List<Sma> sma50s
    Sma sma50
    List<Sma> sma100s
    Sma sma100
    List<Ema> emas
    Ema ema
    List<Macd> macds
    Macd macd
    List<BollingerBand> bollingerBands
    BollingerBand bollingerBand
    List<Dmi> dmis
    Dmi dmi
    List<Atr> atrs
    Atr atr
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
        this.sma5s = Tools.indicators(ohlcvs, SmaContext.of(5))
        this.sma5 = sma5s.first()
        this.sma50s = Tools.indicators(ohlcvs, SmaContext.of(50))
        this.sma50 = sma50s.first()
        this.sma100s = Tools.indicators(ohlcvs, SmaContext.of(100))
        this.sma100 = sma100s.first()
        this.emas = Tools.indicators(ohlcvs, EmaContext.DEFAULT)
        this.ema = emas.first()
        this.macds = Tools.indicators(ohlcvs, MacdContext.DEFAULT)
        this.macd = this.macds.first()
        this.bollingerBands = Tools.indicators(ohlcvs, BollingerBandContext.DEFAULT)
        this.bollingerBand = bollingerBands.first()
        this.dmis = Tools.indicators(ohlcvs, DmiContext.DEFAULT)
        this.dmi = this.dmis.first()
        this.atrs = Tools.indicators(ohlcvs, AtrContext.DEFAULT)
        this.atr = atrs.first()
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
        // sma5Over50
        score.sma5Over50 = sma5.value > sma50.value ? 100 : 0
        // sma5Over100
        score.sma5Over100 = sma5.value > sma100.value ? 100 : 0
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
        // rsi: 30 이하인 경우 과매도 판정
        score.rsi = rsis.take(3).any{it.value <= 30} ? 100 : 0
        // cci: -100 이하인 경우 과매도 판정
        score.cci = ccis.take(3).any{it.value <= -100} ? 100 : 0
        // stochastic slow: 20 이하인 경우 과매도 판정
        score.stochasticSlow = stochasticSlows.take(3).any{it.slowK <= 20} ? 100 : 0
        // williams r: -80 이하인 경우 과매도 판정
        score.williamsR = williamsRs.take(3).any{it.value <= -80} ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOverboughtScore() {
        def score = new Score()
        // rsi: 70 이상인 경우 과매수 구간 판정
        score.rsi = rsis.take(3).any{it.value >= 70} ? 100 : 0
        // cci: 100 이상인 경우 과매수 판정
        score.cci = ccis.take(3).any{it.value >= 100} ? 100 : 0
        // stochastic slow: 80 이상인 경우 과매수 판정
        score.stochasticSlow = stochasticSlows.take(3).any{it.slowK >= 80} ? 100 : 0
        // williams r: -20 이상인 경우 과매수 판정
        score.williamsR = williamsRs.take(3).any{it.value >= -20} ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getTrailingStopScore() {
        def score = new Score()
        // atr
        def prevOhlcv = ohlcvs.get(1)
        def prevAtr = atrs.get(1)
        def stopPrice = prevOhlcv.high - (prevAtr.value * 2.0)
        score.atrAtr = ohlcv.close < stopPrice ? 100 : 0
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

    @Override
    Scorable getTrailingStopScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getTrailingStopScore())}
        return scoreGroup
    }
}

// config
log.info("variables: {}", variables)
def tideOhlcvType = Ohlcv.Type.valueOf(variables['tideOhlcvType'])
def tideOhlcvPeriod = Integer.parseInt(variables['tideOhlcvPeriod'])
def waveOhlcvType = Ohlcv.Type.valueOf(variables['waveOhlcvType'])
def waveOhlcvPeriod = Integer.parseInt(variables['waveOhlcvPeriod'])
def rippleOhlcvType = Ohlcv.Type.valueOf(variables['rippleOhlcvType'])
def rippleOhlcvPeriod = Integer.parseInt(variables['rippleOhlcvPeriod'])
def orderEnabled = Boolean.parseBoolean(variables['orderEnabled'])
def basePosition = new BigDecimal(variables['basePosition'])
def sellProfitPercentageThreshold = new BigDecimal(variables['sellProfitPercentageThreshold'])

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

// position 산정
def positionScore = tideAnalysis.getTrendScore().getAverage()
def marginPosition = 1.0 - basePosition
def positionPerScore = (marginPosition/100)
def position = (basePosition + (positionPerScore * positionScore)) as BigDecimal

// tide,wave,ripple 별 average position 산정
def tideAveragePosition = tideAnalysis.applyAveragePosition(position)
def waveAveragePosition = waveAnalysis.applyAveragePosition(position)
def rippleAveragePosition = tideAnalysis.applyAveragePosition(position)
log.info("position: {}", position)
log.info("tideAveragePosition: {}", tideAveragePosition)
log.info("waveAveragePosition: {}", waveAveragePosition)
log.info("rippleAveragePosition: {}", rippleAveragePosition)

// profit percentage
def profitPercentage = balanceAsset?.getProfitPercentage() ?: 0.0
log.info("balanceAsset: {}", balanceAsset)
log.info("profitPercentage: {}", profitPercentage)

// message 정의
def message = """
position:${position} (tide:${tideAveragePosition}|wave:${waveAveragePosition}|ripple:${rippleAveragePosition})
tide.mom:${tideAnalysis.getMomentumScore().getAverage()}|tre:${tideAnalysis.getTrendScore().getAverage()}
+ sma5:${tideAnalysis.sma5.value}|sma50:${tideAnalysis.sma50.value}|sma100:${tideAnalysis.sma100.value}|macd:${tideAnalysis.macd.value}
wave.mon:${waveAnalysis.getMomentumScore().getAverage()}|vol:${waveAnalysis.getVolatilityScore().getAverage()}|osd:${waveAnalysis.getOversoldScore().getAverage()}|obt:${waveAnalysis.getOverboughtScore().getAverage()}|tst:${waveAnalysis.getTrailingStopScore().getAverage()}
+ adx:${waveAnalysis.dmi.adx}|rsi:${waveAnalysis.rsi.value}|sto:${waveAnalysis.stochasticSlow.slowK}|cci:${waveAnalysis.cci.value}|wil:${waveAnalysis.williamsR.value}
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
            strategyResult = StrategyResult.of(Action.BUY, waveAveragePosition, "[WAVE OVERSOLD BUY] " + message)
        }
    }
    // 중기 과매수 상태
    if (waveAnalysis.getOverboughtScore() > 50) {
        // 단기 하락 모멘텀
        if (rippleAnalysis.getMomentumScore() < 20) {
            // 매도 포지션
            strategyResult = StrategyResult.of(Action.SELL, waveAveragePosition, "[WAVE OVERBOUGHT SELL] " + message)
            // filter - sell profit percentage threshold
            if (sellProfitPercentageThreshold > 0.0) {
                if (profitPercentage < sellProfitPercentageThreshold) {
                    strategyResult = null
                }
            }
        }
    }
}

// trend score 가 미달 하는 경우 trailing stop 체크
if (tideAnalysis.getTrendScore() < 20) {
    // 추가로 장기,중기,단기 모두 하락 모멘텀 시 반등 여지가 없다고 판단 하고 매도 포지션
    if (tideAnalysis.getMomentumScore() < 20 && waveAnalysis.getMomentumScore() < 20 && rippleAnalysis.getMomentumScore() < 20) {
        // 중기 trailing stop + 장기 average position 기준 으로 매도
        if (waveAnalysis.getTrailingStopScore() > 50) {
            strategyResult = StrategyResult.of(Action.SELL, tideAveragePosition, "[TIDE LOWER TREND + WAVE TAILING STOP SELL] " + message)
        }
    }
}

// orderEnabled 설정 이 true 가 아닐 경우는 실제 주문 제외
log.info("orderEnabled: {}", orderEnabled)
if (!orderEnabled) {
    log.info("override strategyResult to be null")
    strategyResult = null
}

// return
return strategyResult
