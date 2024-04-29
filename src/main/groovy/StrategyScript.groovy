import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.trade.Tool
import org.oopscraft.fintics.indicator.*

import java.time.LocalTime

interface Scorable {
    Number getAverage()
}

class Score extends LinkedHashMap<String, BigDecimal> implements Scorable {
    Number getAverage() {
        return this.values().empty ? 0 : this.values().average() as Number
    }
    @Override
    String toString() {
        return super.toString()
    }
}

interface Analyzable {
    Scorable getDirectionScore()
    Scorable getMomentumScore()
    Scorable getVolatilityScore()
    Scorable getUnderestimateScore()
    Scorable getOverestimateScore()
    Scorable getOversoldScore()
    Scorable getOverboughtScore()
}

class Analysis implements Analyzable {
    List<Ohlcv> ohlcvs
    Ohlcv ohlcv
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
        this.macds = Tool.indicators(ohlcvs, MacdContext.DEFAULT)
        this.macd = this.macds.first()
        this.dmis = Tool.indicators(ohlcvs, DmiContext.DEFAULT)
        this.dmi = this.dmis.first()
        this.rsis = Tool.indicators(ohlcvs, RsiContext.DEFAULT)
        this.rsi = rsis.first()
        this.atrs = Tool.indicators(ohlcvs, AtrContext.DEFAULT)
        this.atr = atrs.first()
        this.bollingerBands = Tool.indicators(ohlcvs, BollingerBandContext.DEFAULT)
        this.bollingerBand = bollingerBands.first()
        this.obvs = Tool.indicators(ohlcvs, ObvContext.DEFAULT)
        this.obv = obvs.first()
        this.chaikinOscillators = Tool.indicators(ohlcvs, ChaikinOscillatorContext.DEFAULT)
        this.chaikinOscillator = chaikinOscillators.first()
        this.ccis = Tool.indicators(ohlcvs, CciContext.DEFAULT)
        this.cci = ccis.first()
        this.stochasticSlows = Tool.indicators(ohlcvs, StochasticSlowContext.DEFAULT)
        this.stochasticSlow = stochasticSlows.first()
    }

    @Override
    Scorable getDirectionScore() {
        def score = new Score()
        // macd
        score.macdValueOverSignal = macd.value > macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
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
        // cci
        score.cciValue = cci.value > 0 ? 100 : 0
        // dmi
        score.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
        // obv
        score.obvValueOverSignal = obv.value > obv.signal ? 100 : 0
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
    Scorable getUnderestimateScore() {
        def score = new Score()
        // macd
        score.macdValue = macd.value < 0 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOverestimateScore() {
        def score = new Score()
        // macd
        score.macdValue = macd.value > 0 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOversoldScore() {
        def score = new Score()
        // rsi
        score.rsiValue = rsi.value <= 30 ? 100 : 0
        // cci
        score.cciValue = cci.value <= -100 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOverboughtScore() {
        def score = new Score()
        // rsi
        score.rsiValue = rsi.value >= 70 ? 100 : 0
        // cci
        score.cciValue = cci.value >= 100 ? 100 : 0
        // return
        return score
    }

    @Override
    String toString() {
        return [
                directionScore: "${this.getDirectionScore()}",
                momentumScore: "${this.getMomentumScore()}",
                volatilityScore: "${this.getVolatilityScore()}",
                overboughtScore: "${this.getOverboughtScore()}",
                oversoldScore: "${this.getOversoldScore()}"
        ].toString()
    }
}

//================================
// define
//================================
// config
def waveOhlcvType = variables['waveOhlcvType'] as Ohlcv.Type
def waveOhlcvPeriod = variables['waveOhlcvPeriod'] as Integer
def tideOhlcvType = variables['tideOhlcvType'] as Ohlcv.Type
def tideOhlcvPeriod = variables['tideOhlcvPeriod'] as Integer
log.info("waveOhlcvType(Period): {}({})", waveOhlcvType, waveOhlcvPeriod)
log.info("tideOhlcvType(Period): {}({})", tideOhlcvType, tideOhlcvPeriod)

// default
def hold = null
List<Ohlcv> ohlcvs = assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, 1)

// ripple
def analysis = new Analysis(ohlcvs)
log.info("analysis: {}", analysis)

// wave
def waveAnalysis = new Analysis(assetProfile.getOhlcvs(waveOhlcvType, waveOhlcvPeriod))
log.info("waveAnalysis: {}", waveAnalysis)

// tide
def tideAnalysis = new Analysis(assetProfile.getOhlcvs(tideOhlcvType, tideOhlcvPeriod))
log.info("tideAnalysis: {}", tideAnalysis)

//================================
// trade
//================================
// buy
if (analysis.getDirectionScore().getAverage() > 75 && analysis.getMomentumScore().getAverage() > 75) {
    // default
    hold = 1.0
    // filter - volatility
    if (waveAnalysis.getVolatilityScore().getAverage() < 75) {
        hold = null
    }
    // filter - overestimate
    if (waveAnalysis.getOverestimateScore().getAverage() > 75) {
        hold = null
    }
}
// sell
if (analysis.getDirectionScore().getAverage() < 25 && analysis.getMomentumScore().getAverage() < 25) {
    // default
    hold = 0.9
    // filter - volatility
    if (waveAnalysis.getVolatilityScore().getAverage() < 75) {
        hold = null
    }
    // filter - underestimate
    if (waveAnalysis.getUnderestimateScore().getAverage() > 75) {
        hold = null
    }
}

//================================
// fallback
//================================
// tide direction and momentum
if (tideAnalysis.getDirectionScore().getAverage() < 50 && tideAnalysis.getMomentumScore().getAverage() < 50) {
    hold = 0
}

//================================
// return
//================================
return hold
