import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.trade.Tool
import org.oopscraft.fintics.indicator.*

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
    BigDecimal getZScore()
    Scorable getBullishScore()
    Scorable getBearishScore()
    Scorable getVolatilityScore()
    Scorable getMomentumScore()
    Scorable getUnderestimateScore()
    Scorable getOverestimateScore()
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
    BigDecimal getZScore() {
        def zScores = Tool.zScores(ohlcvs.take(10).collect{it.closePrice})
        return zScores.first()
    }

    @Override
    Scorable getBullishScore() {
        def score = new Score()
        // macd
        score.macdValueOverSignal = macd.value > macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
        // dmi
        score.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
        // rsi
        score.rsiValueOverSignal = rsi.value > rsi.signal ? 100 : 0
        // bollinger band
        score.bollingerBandPriceOverMiddle = ohlcv.closePrice > bollingerBand.middle ? 100 : 0
        // obv
        score.obvValueOverSignal = obv.value > obv.signal ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValueOverSignal = chaikinOscillator.value > chaikinOscillator.signal ? 100 : 0
        // cci
        score.cciValueOverSignal = cci.value > cci.signal ? 100 : 0
        // stochastic slow
        score.stochasticSlowKOverD = stochasticSlow.slowK > stochasticSlow.slowD ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getBearishScore() {
        def score = new Score()
        // macd
        score.macdValueUnderSignal = macd.value < macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator < 0 ? 100 : 0
        // dmi
        score.dmiValuePdiUnderMdi = dmi.pdi < dmi.mdi ? 100 : 0
        // rsi
        score.rsiValueUnderSignal = rsi.value < rsi.signal ? 100 : 0
        // bollinger band
        score.bollingerBandPriceUnderMiddle = ohlcv.closePrice < bollingerBand.middle ? 100 : 0
        // obv
        score.obvValueUnderSignal = obv.value < obv.signal ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValueUnderSignal = chaikinOscillator.value < chaikinOscillator.signal ? 100 : 0
        // cci
        score.cciValueUnderSignal = cci.value < cci.signal ? 100 : 0
        // stochastic slow
        score.stochasticSlowKUnderD = stochasticSlow.slowK < stochasticSlow.slowD ? 100 : 0
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
    Scorable getMomentumScore() {
        def score = new Score()
        // macd
        score.macdValue = macd.value > 0 ? 100 : 0
        // rsi
        score.rsiValue = rsi.value > 50 ? 100 : 0
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
    String toString() {
        return [
                bullishScore: "${this.getBullishScore()}",
                bearishScore: "${this.getBearishScore()}",
                volatilityScore: "${this.getVolatilityScore()}",
                momentumScore: "${this.getMomentumScore()}",
                underestimateScore: "${this.getUnderestimateScore()}",
                overestimateScore: "${this.getOverestimateScore()}"
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
def analysis = new Analysis(ohlcvs)

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
if (analysis.getZScore() > 1.5 && analysis.getBullishScore().getAverage() > 70) {
    if (waveAnalysis.getBullishScore().getAverage() > 70) {
        // default
        hold = 1
        // volatility
        if (waveAnalysis.getVolatilityScore().getAverage() < 70) {
            hold = null
        }
        // overestimate
        if (waveAnalysis.getOverestimateScore().getAverage() > 70) {
            hold = null
        }
    }
}
// sell
if (analysis.getZScore() < -1.5 && analysis.getBearishScore().getAverage() > 70) {
    if (waveAnalysis.getBearishScore().getAverage() > 70) {
        // default
        hold = 0
        // momentum
        if (waveAnalysis.getMomentumScore().getAverage() > 70) {
            hold = null
        }
    }
}

//================================
// fallback
//================================
// tide is bearish, disable
if (tideAnalysis.getBearishScore().getAverage() > 70) {
    hold = 0
}

//================================
// return
//================================
return hold
