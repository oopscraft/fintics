import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.trade.Tool

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
    Scorable getBullishScore()
    Scorable getBearishScore()
    Scorable getVolatilityScore()
    Scorable getMomentumScore()
    Scorable getTrailingStopScore(multiplier)
    BigDecimal getAveragePrice()
}

class Analysis implements Analyzable {
    List<Ohlcv> ohlcvs
    Ohlcv ohlcv
    List<Ema> emas
    Ema ema
    List<Macd> macds
    Macd macd
    List<Rsi> rsis
    Rsi rsi
    List<Dmi> dmis
    Dmi dmi
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
        this.emas = Tool.calculate(ohlcvs, EmaContext.of(30))
        this.ema = this.emas.first()
        this.macds = Tool.calculate(ohlcvs, MacdContext.DEFAULT)
        this.macd = this.macds.first()
        this.rsis = Tool.calculate(ohlcvs, RsiContext.DEFAULT)
        this.rsi = this.rsis.first()
        this.dmis = Tool.calculate(ohlcvs, DmiContext.DEFAULT)
        this.dmi = this.dmis.first()
        this.atrs = Tool.calculate(ohlcvs, AtrContext.DEFAULT)
        this.atr = this.atrs.first()
        this.bollingerBands = Tool.calculate(ohlcvs, BollingerBandContext.DEFAULT)
        this.bollingerBand = this.bollingerBands.first()
        this.obvs = Tool.calculate(ohlcvs, ObvContext.DEFAULT)
        this.obv = this.obvs.first()
        this.chaikinOscillators = Tool.calculate(ohlcvs, ChaikinOscillatorContext.DEFAULT)
        this.chaikinOscillator = this.chaikinOscillators.first()
        this.ccis = Tool.calculate(ohlcvs, CciContext.DEFAULT)
        this.cci = ccis.first()
        this.stochasticSlows = Tool.calculate(ohlcvs, StochasticSlowContext.DEFAULT)
        this.stochasticSlow = stochasticSlows.first()
    }

    @Override
    Scorable getBullishScore() {
        def score = new Score()
        // macd
        score.macdValueOverSignal = macd.value > macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
        // rsi
        score.rsiValueOverSignal = rsi.value > rsi.signal ? 100 : 0
        // obv
        score.obvValueOverSignal = obv.value > obv.signal ? 100 : 0
        // cci
        score.cciValueOverSignal = cci.value > cci.signal ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValueOverSignal = chaikinOscillator.value > chaikinOscillator.signal ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getBearishScore() {
        def score = new Score()
        // macd
        score.macdValueUnderSignal = macd.value < macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator < 0 ? 100 : 0
        // rsi
        score.rsiValueUnderSignal = rsi.value < rsi.signal ? 100 : 0
        // obv
        score.obvValueUnderSignal = obv.value < obv.signal ? 100 : 0
        // cci
        score.cciValueUnderSignal = cci.value < cci.signal ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValueUnderSignal = chaikinOscillator.value < chaikinOscillator.signal ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getVolatilityScore() {
        def score = new Score()
        // dmi
        def dmiAdxAverage = dmis.take(60).collect{it.adx}.average() as BigDecimal
        score.dmiAdx = dmi.adx > dmiAdxAverage ? 100 : 0
        // atr
        def atrValueAverage = atrs.take(60).collect{it.value}.average() as BigDecimal
        score.atrValue = atr.value > atrValueAverage ? 100 : 0
        // bollinger band
        def bollingerBandWidthAverage = bollingerBands.take(60).collect{it.width}.average() as BigDecimal
        score.bollingerBandWidth = bollingerBand.width > bollingerBandWidthAverage ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getMomentumScore() {
        def score = new Score()
        // ema
        score.emaValue = ohlcv.closePrice > ema.value ? 100 : 0
        // bollinger band
        score.bollingerBandPriceOverMiddle = ohlcv.closePrice > bollingerBand.middle ? 100 : 0
        // macd
        score.macdValue = macd.value > 0 ? 100 : 0
        // rsi
        score.rsiValue = rsi.value > 50 ? 100 : 0
        // cci
        score.cciValue = cci.value > 0 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getTrailingStopScore(multiplier) {
        def score = new Score()
        // ATR based trailing stop loss
        Ohlcv prevOhlcv = ohlcvs.get(1)
        Atr prevAtr = atrs.get(1)
        score.atrValue = ohlcv.closePrice < prevOhlcv.highPrice - (prevAtr.value * multiplier) ? 100 : 0
        // return
        return score
    }

    @Override
    BigDecimal getAveragePrice() {
        return ohlcvs.take(120).collect{it.closePrice}.average() as BigDecimal
    }

    @Override
    String toString() {
        return [
                bullishScore: "${this.getBullishScore()}",
                bearishScore: "${this.getBearishScore()}",
                volatilityScore: "${this.getVolatilityScore()}"
        ].toString()
    }
}

//================================
// define
//================================
// config
def waveOhlcvType = config['waveOhlcvType'] as Ohlcv.Type
def waveOhlcvPeriod = config['waveOhlcvPeriod'] as Integer
def tideOhlcvType = config['tideOhlcvType'] as Ohlcv.Type
def tideOhlcvPeriod = config['tideOhlcvPeriod'] as Integer
log.info("waveOhlcvType(Period): {}({})", waveOhlcvType, waveOhlcvPeriod)
log.info("tideOhlcvType(Period): {}({})", tideOhlcvType, tideOhlcvPeriod)

// default
def hold = null
List<Ohlcv> ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1).findAll{it.dateTime.toLocalDate().isEqual(dateTime.toLocalDate())}
def ohlcv = ohlcvs.first()
def priceZScores = Tool.zScores(ohlcvs.take(10).collect{it.closePrice})
def priceZScore = priceZScores.first()
def analysis = new Analysis(ohlcvs)
log.info("analysis: {}", analysis)

// wave
def waveAnalysis = new Analysis(assetIndicator.getOhlcvs(waveOhlcvType, waveOhlcvPeriod))

// tide
def tideAnalysis = new Analysis(assetIndicator.getOhlcvs(tideOhlcvType, tideOhlcvPeriod))

//================================
// trade
//================================
// buy
if (priceZScore > 1.5 && analysis.getBullishScore().getAverage() > 70) {
    // wave is bullish
    if (waveAnalysis.getBullishScore().getAverage() > 70) {
        // default
        hold = 1
        // volatility
        if (waveAnalysis.getVolatilityScore().getAverage() < 70) {
            hold = null
        }
    }
}
// sell
if (priceZScore < -1.5 && analysis.getBearishScore().getAverage() > 70) {
    // wave is bearish
    if (waveAnalysis.getBearishScore().getAverage() > 70) {
        // default
        hold = 0
        // momentum
        if (waveAnalysis.getMomentumScore().getAverage() > 70) {
            hold = null
        }
        // trailing stop
        if (waveAnalysis.getTrailingStopScore(3).getAverage() > 70) {
            hold = 0
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

