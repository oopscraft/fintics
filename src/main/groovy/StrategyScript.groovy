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
    Scorable getOverboughtScore()
    Scorable getOversoldScore()
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
        def score = new Score();
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
    Scorable getOverboughtScore() {
        def score = new Score()
        // cci
        score.cciValue = cci.value >= 100 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOversoldScore() {
        def score = new Score()
        // cci
        score.cciValue = cci.value <= -100 ? 100 : 0
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
def overnight = (variables['overnight'] as Boolean) ?: false
log.info("waveOhlcvType(Period): {}({})", waveOhlcvType, waveOhlcvPeriod)
log.info("tideOhlcvType(Period): {}({})", tideOhlcvType, tideOhlcvPeriod)

// default
def hold = null
List<Ohlcv> ohlcvs = assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, 1)

// filter z-score
def priceZScore = Tool.zScore(ohlcvs.take(10).collect{it.closePrice})
if (priceZScore.abs() < 1.0) {
    log.info("skip - priceZScore: {}", priceZScore)
    return null
}

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
if (analysis.getDirectionScore().getAverage() > 75) {
    // wave is bullish
    if (waveAnalysis.getDirectionScore().getAverage() > 75) {
        // default
        hold = 1
        // filter - momentum
        if (waveAnalysis.getMomentumScore().getAverage() < 75) {
            hold = null
        }
        // filter - volatility
        if (waveAnalysis.getVolatilityScore().getAverage() < 75) {
            hold = null
        }
        // filter - overbought
        if (waveAnalysis.getOverboughtScore().getAverage() > 75) {
            hold = null
        }
    }
}
// sell
if (analysis.getDirectionScore().getAverage() < 25) {
    // wave is bearish
    if (waveAnalysis.getDirectionScore().getAverage() < 25) {
        // default
        hold = 0
    }
}

//================================
// fallback
//================================
// tide direction
if (tideAnalysis.getDirectionScore().getAverage() < 75) {
    hold = 0
}
// tide momentum
if (tideAnalysis.getMomentumScore().getAverage() < 75) {
    hold = 0
}
// overnight
if (!overnight) {
    if (dateTime.toLocalTime().isAfter(LocalTime.of(15, 10))) {
        hold = 0
    }
}

//================================
// return
//================================
return hold
