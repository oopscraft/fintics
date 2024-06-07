import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.model.StrategyResult
import org.oopscraft.fintics.model.StrategyResult.Action
import org.oopscraft.fintics.trade.Tools
import org.oopscraft.fintics.indicator.*

interface Scorable {
    Number getAverage()
}

class ScoreGroup extends LinkedHashMap<String, Scorable> implements Scorable {
    @Override
    Number getAverage() {
        return this.values().collect{it.getAverage()}.average() as Number
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
    Scorable getEstimateScore()
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
    List<Dmi> dmis
    Dmi dmi
    List<Rsi> rsis
    Rsi rsi
    List<Atr> atrs
    Atr atr
    List<BollingerBand> bollingerBands
    BollingerBand bollingerBand
    List<Obv> obvs
    Obv obv
    List<ChaikinOscillator> chaikinOscillators
    ChaikinOscillator chaikinOscillator
    List<Cci> ccis
    Cci cci
    List<StochasticSlow> stochasticSlows
    StochasticSlow stochasticSlow

    Analysis(List<Ohlcv> ohlcvs) {
        this.ohlcvs = ohlcvs
        this.ohlcv = this.ohlcvs.first()
        this.emas = Tools.indicators(ohlcvs, EmaContext.DEFAULT)
        this.ema = this.emas.first()
        this.macds = Tools.indicators(ohlcvs, MacdContext.DEFAULT)
        this.macd = this.macds.first()
        this.dmis = Tools.indicators(ohlcvs, DmiContext.DEFAULT)
        this.dmi = this.dmis.first()
        this.rsis = Tools.indicators(ohlcvs, RsiContext.DEFAULT)
        this.rsi = rsis.first()
        this.atrs = Tools.indicators(ohlcvs, AtrContext.DEFAULT)
        this.atr = atrs.first()
        this.bollingerBands = Tools.indicators(ohlcvs, BollingerBandContext.DEFAULT)
        this.bollingerBand = bollingerBands.first()
        this.obvs = Tools.indicators(ohlcvs, ObvContext.DEFAULT)
        this.obv = obvs.first()
        this.chaikinOscillators = Tools.indicators(ohlcvs, ChaikinOscillatorContext.DEFAULT)
        this.chaikinOscillator = chaikinOscillators.first()
        this.ccis = Tools.indicators(ohlcvs, CciContext.DEFAULT)
        this.cci = ccis.first()
        this.stochasticSlows = Tools.indicators(ohlcvs, StochasticSlowContext.DEFAULT)
        this.stochasticSlow = stochasticSlows.first()
    }

    @Override
    Scorable getMomentumScore() {
        def score = new Score()
        // ohlcv
        score.ohlcvClosePricePctChange = Tools.pctChange(ohlcvs.take(period).collect{it.closePrice}) > 0.0 ? 100 : 0
        // ema
        score.emaPriceOverValue = ohlcv.closePrice > ema.value ? 100 : 0
        score.emaValuePctChange = Tools.pctChange(emas.take(period).collect{it.value}) > 0.0 ? 100 : 0
        // macd
        score.macdValueOverSignal = macd.value > macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
        score.macdValue = macd.value > 0 ? 100 : 0
        // rsi
        score.rsiValueOverSignal = rsi.value > rsi.signal ? 100 : 0
        score.rsiValue = rsi.value > 50 ? 100 : 0
        // cci
        score.cciValueOverSignal = cci.value > cci.signal ? 100 : 0
        score.cciValue = cci.value > 0 ? 100 : 0
        // dmi
        score.dmiPdiPctChange = Tools.pctChange(dmis.take(period).collect{it.pdi}) > 0 ? 100 : 0
        score.dmiMdiPctChange = Tools.pctChange(dmis.take(period).collect{it.mdi}) < 0 ? 100 : 0
        score.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
        // obv
        score.obvValueOverSignal = obv.value > obv.signal ? 100 : 0
        score.obvPctChange = Tools.pctChange(obvs.take(period).collect{it.value}) > 0 ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValueOverSignal = chaikinOscillator.value > chaikinOscillator.signal ? 100 : 0
        score.chaikinOscillatorValue = chaikinOscillator.value > 0 ? 100 : 0
        // stochastic slow
        score.stochasticSlowKOverD = stochasticSlow.slowK > stochasticSlow.slowD ? 100 : 0
        score.stochasticSlowK = stochasticSlow.slowK > 50 ? 100 : 0
        // bollinger band
        score.bollignerBandPriceOverMiddle = ohlcv.closePrice > bollingerBand.middle ? 100 : 0
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
    Scorable getEstimateScore() {
        def score = new Score()
        // ema
        score.emaValue = ohlcv.closePrice > ema.value ? 100 : 0
        // bollinger band
        score.bollingerBandPriceOverMiddle = ohlcv.closePrice > bollingerBand.middle ? 100 : 0
        // cci
        score.cciValue = cci.value < 0 ? 100 : 0
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
                estimateScore: "${this.getEstimateScore()}",
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
    Scorable getEstimateScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getEstimateScore())}
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
def ohlcvPeriod = variables['ohlcvPeriod'] as Integer
def waveOhlcvType = variables['waveOhlcvType'] as Ohlcv.Type
def waveOhlcvPeriod = variables['waveOhlcvPeriod'] as Integer
def tideOhlcvType = variables['tideOhlcvType'] as Ohlcv.Type
def tideOhlcvPeriod = variables['tideOhlcvPeriod'] as Integer
def basePosition = variables['basePosition'] as BigDecimal
def targetReturn = variables['targetReturn'] as BigDecimal
def stopLoss = variables['stopLoss'] as BigDecimal

// default
StrategyResult strategyResult = null
List<Ohlcv> ohlcvs = assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, ohlcvPeriod)
def profitPercentage = balanceAsset?.getProfitPercentage() ?: 0.0

// ripple
def analysis = new Analysis(ohlcvs)

// wave
def waveAnalysis = new Analysis(assetProfile.getOhlcvs(waveOhlcvType, waveOhlcvPeriod))

// tide
def tideAnalysis = new Analysis(assetProfile.getOhlcvs(tideOhlcvType, tideOhlcvPeriod))

// logging
log.info("profitPercentage: {}", profitPercentage)
log.info("analysis.momentum: {}", analysis.getMomentumScore())
log.info("wave.momentum: {}", waveAnalysis.getMomentumScore())
log.info("wave.volatility: {}", waveAnalysis.getVolatilityScore())
log.info("wave.estimate: {}", waveAnalysis.getEstimateScore())
log.info("wave.oversold: {}", waveAnalysis.getOversoldScore())
log.info("wave.overbought: {}", waveAnalysis.getOverboughtScore())
log.info("tide.momentum: {}", tideAnalysis.getMomentumScore())
log.info("tide.estimate: {}", tideAnalysis.getEstimateScore())
log.info("tide.oversold: {}", tideAnalysis.getOversoldScore())
log.info("tide.overbought: {}", tideAnalysis.getOverboughtScore())

//================================
// trade
//================================
// buy - 단기 상승 시
if (analysis.getMomentumScore().getAverage() > 75) {
    // 1. 중기 과매도 상태인 경우 매수 포지션 설정
    if (waveAnalysis.getOversoldScore().getAverage() > 50) {
        strategyResult = StrategyResult.of(Action.BUY, 1.0, "wave.oversold: ${waveAnalysis.getOversoldScore()}")
    }
    // 2. 장기 추세 상승 + 중기 조정 시 매수 포지션 설정
    if (tideAnalysis.getMomentumScore().getAverage() > 75 && waveAnalysis.getEstimateScore().getAverage() < 50) {
        strategyResult = StrategyResult.of(Action.BUY, 1.0, "wave.momentum: ${waveAnalysis.getMomentumScore()}\nwave.estimate: ${waveAnalysis.getEstimateScore()}")
    }
    // filter - 변동성 없을 경우 제외
    if (waveAnalysis.getVolatilityScore().getAverage() < 50) {
        strategyResult = null
    }
    // filter - 장기 과매수 상태인 경우 제외
    if (tideAnalysis.getOverboughtScore().getAverage() > 50) {
        strategyResult = null;
    }
    // filter - 장기 하락 추세인 경우 제외
    if (tideAnalysis.getMomentumScore().getAverage() < 25) {
        strategyResult = null
    }
}
// sell - 단기 하락 시
if (analysis.getMomentumScore().getAverage() < 25) {
    // 1. 중기 과매수 상태인 경우 매도 포지션 설정
    if (waveAnalysis.getOverboughtScore().getAverage() > 50) {
        strategyResult = StrategyResult.of(Action.SELL, basePosition, "wave.overbought: ${waveAnalysis.getOverboughtScore()}")
    }
    // 2. 장기 추세 하락 + 중기 반등 시 매도 포지션 설정
    if (tideAnalysis.getMomentumScore().getAverage() < 25 && waveAnalysis.getEstimateScore().getAverage() > 50) {
        strategyResult = StrategyResult.of(Action.SELL, basePosition, "wave.momemtum: ${waveAnalysis.getMomentumScore()}\nwave.estimate: ${waveAnalysis.getEstimateScore()}")
    }
    // filter - 중기 변동성 없을 경우 제외
    if (waveAnalysis.getVolatilityScore().getAverage() < 50) {
        strategyResult = null
    }
    // filter - 장기 과매도 상태인 경우 제외
    if (tideAnalysis.getOversoldScore().getAverage() > 50) {
        strategyResult = null
    }
    // override - 목표 수익률 (targetReturn) 설정 시 도달 하지 못한 경우 제외
    if (targetReturn > 0.0) {
        if (profitPercentage < targetReturn) {
            strategyResult = null
        }
    }
    // override - 손실 제한 (stopLoss) 설정 시 이하로 하락 시 강제 매도
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
