import java.lang.Math
import org.jetbrains.annotations.NotNull
import org.oopscraft.fintics.indicator.*
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.model.TradeAsset
import org.oopscraft.fintics.strategy.StrategyResult
import org.oopscraft.fintics.strategy.StrategyResult.Action
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
    BigDecimal getAveragePosition(BigDecimal position)
    Scorable getMomentumScore()
    Scorable getVolatilityScore()
    Scorable getOversoldScore()
    Scorable getOverboughtScore()
    Scorable getTrailingStopScore()
}

class Analysis implements Analyzable {
    List<Ohlcv> ohlcvs
    Ohlcv ohlcv
    List<Sma> smas
    Sma sma
    List<Ema> emas
    Ema ema
    List<Macd> macds
    Macd macd
    List<BollingerBand> bollingerBands
    BollingerBand bollingerBand
    List<Dmi> dmis
    Dmi dmi
    List<ChaikinOscillator> chaikinOscillators
    ChaikinOscillator chaikinOscillator
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

    Analysis(TradeAsset profile, Ohlcv.Type type, int period) {
        this.ohlcvs = profile.getOhlcvs(type, period)
        this.ohlcv = this.ohlcvs.first()
        this.smas = Tools.indicators(ohlcvs, SmaContext.DEFAULT)
        this.sma = smas.first()
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
    BigDecimal getAveragePosition(BigDecimal position) {
        def averagePrice = this.getAverageClose()
        def currentPrice = this.getCurrentClose()
        def averageWeight = averagePrice/currentPrice as BigDecimal
        def averagePosition = ((position * averageWeight) as BigDecimal)
                .setScale(2, RoundingMode.HALF_UP)
        return averagePosition
    }

    @Override
    Scorable getMomentumScore() {
        def score = new Score()
        // sma
        score.smaPriceOverValue = ohlcv.close > sma.value ? 100 : 0
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
        // stochastic slow
        score.stochasticSlowK = stochasticSlow.slowK > 50 ? 100 : 0
        // williams r
        score.williamsRValue = williamsR.value > -50 ? 100 : 0
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
    BigDecimal getAveragePosition(BigDecimal position) {
        return this.values().collect{it.getAverageClose()}.average() as Number
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

    @Override
    Scorable getTrailingStopScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getTrailingStopScore())}
        return scoreGroup
    }
}

//===============================
// config
//===============================
StrategyResult strategyResult = null
log.info("variables: {}", variables)
def orderEnabled = Boolean.parseBoolean(variables['orderEnabled'])
def tideOhlcvType = Ohlcv.Type.valueOf(variables['tideOhlcvType'])
def tideOhlcvPeriod = Integer.parseInt(variables['tideOhlcvPeriod'])
def waveOhlcvType = Ohlcv.Type.valueOf(variables['waveOhlcvType'])
def waveOhlcvPeriod = Integer.parseInt(variables['waveOhlcvPeriod'])
def rippleOhlcvType = Ohlcv.Type.valueOf(variables['rippleOhlcvType'])
def rippleOhlcvPeriod = Integer.parseInt(variables['rippleOhlcvPeriod'])
def basePosition = new BigDecimal(variables['basePosition'])
def sellProfitPercentageThreshold = new BigDecimal(variables['sellProfitPercentageThreshold'])
def splitIndex = Integer.parseInt(variables['splitIndex'] ?: '0')

//===============================
// analysis
//===============================
def tideAnalysis = new Analysis(tradeAsset, tideOhlcvType, tideOhlcvPeriod)
def waveAnalysis = new Analysis(tradeAsset, waveOhlcvType, waveOhlcvPeriod)
def rippleAnalysis = new Analysis(tradeAsset, rippleOhlcvType, rippleOhlcvPeriod)

//===============================
// split ranges
//===============================
def splitPeriod = 100
def splitSize = 5
def splitOhlcvs = tradeAsset.getOhlcvs(Ohlcv.Type.DAILY, 1).take(splitPeriod)
def splitMaxPrice = splitOhlcvs.collect{it.high}.max()
def splitMinPrice = splitOhlcvs.collect{it.low}.min()
def splitInterval = ((splitMaxPrice - splitMinPrice)/splitSize as BigDecimal).setScale(4, RoundingMode.HALF_UP)
def splitLimitPrices = (0..splitSize-1).collect {
    splitMaxPrice - (it * splitInterval) as BigDecimal
}
def splitLimitPrice = splitLimitPrices[splitIndex]
def splitBuyLimited = false
// splitIndex 가 0 이상 설정된 경우
if (splitIndex > 0) {
    // 현제 가격이 split limit 이상인 경우 분할 매수 제한
    if (rippleAnalysis.getCurrentClose() > splitLimitPrice) {
        splitBuyLimited = true
    }
}

//===============================
// position
//===============================
def positionScore = (tideAnalysis.getMomentumScore().getAverage() - 50).max(0)*2
def positionPerScore = (1.0 -basePosition)/100
def position = basePosition + (positionPerScore * positionScore) as BigDecimal

//===============================
// profit percentage
//===============================
def profitPercentage = balanceAsset?.getProfitPercentage() ?: 0.0

//===============================
// message
//===============================
def message = """
splitLimits:${splitLimitPrices}
splitIndex:${splitIndex}
splitLimit:${splitLimitPrice}
splitBuyLimited:${splitBuyLimited}
position:${position.toPlainString()}
tide.momentum:${tideAnalysis.getMomentumScore().toString()}
tide.oversold:${tideAnalysis.getOversoldScore().toString()}
tide.overbought:${tideAnalysis.getOverboughtScore().toString()}
- rsi:${tideAnalysis.rsi.value}|sto:${tideAnalysis.stochasticSlow.slowK}|cci:${tideAnalysis.cci.value}|wil:${tideAnalysis.williamsR.value}
wave.volatility:${waveAnalysis.getVolatilityScore().toString()}
- adx:${waveAnalysis.dmi.adx}
wave.oversold:${waveAnalysis.getOversoldScore().toString()}
wave.overbought:${waveAnalysis.getOverboughtScore().toString()}
- rsi:${waveAnalysis.rsi.value}|sto:${waveAnalysis.stochasticSlow.slowK}|cci:${waveAnalysis.cci.value}|wil:${waveAnalysis.williamsR.value}
ripple.momentum:${rippleAnalysis.getMomentumScore().toString()}
"""
log.info("message: {}", message)
tradeAsset.setMessage(message)

//===============================
// trade
//===============================
// wave volatility
if (waveAnalysis.getVolatilityScore() > 50) {
    // wave oversold
    if (waveAnalysis.getOversoldScore() > 50) {
        // ripple bullish momentum
        if (rippleAnalysis.getMomentumScore() > 50) {
            // buy
            def buyAveragePosition = waveAnalysis.getAveragePosition(position)
            strategyResult = StrategyResult.of(Action.BUY, buyAveragePosition, "[WAVE OVERSOLD BUY] " + message)
        }
    }
    // wave overbought
    if (waveAnalysis.getOverboughtScore() > 50) {
        // ripple bearish momentum
        if (rippleAnalysis.getMomentumScore() < 50) {
            // sell
            def sellAveragePosition = waveAnalysis.getAveragePosition(position)
            strategyResult = StrategyResult.of(Action.SELL, sellAveragePosition, "[WAVE OVERBOUGHT SELL] " + message)
        }
    }
}

//===============================
// check sell option
//===============================
if (strategyResult != null && strategyResult.action == Action.BUY) {
    // 현재 split limit 가 활성화 된 경우 매수 제외
    if (splitBuyLimited) {
        strategyResult = null
    }
}

//===============================
// check sell option
//===============================
if (strategyResult != null && strategyResult.action == Action.SELL) {
    // 목표 수익률 이하 매도 제한이 설정된 경우 매도 제외
    if (profitPercentage < sellProfitPercentageThreshold) {
        log.info("profitPercentage under {}", profitPercentage.toPlainString())
        strategyResult = null
    }
}

//================================
// order enabled
//================================
// orderEnabled 설정 이 true 가 아닐 경우는 실제 주문 제외
log.info("orderEnabled: {}", orderEnabled)
if (!orderEnabled) {
    log.info("override strategyResult to be null")
    strategyResult = null
}

//================================
// return
//================================
return strategyResult
