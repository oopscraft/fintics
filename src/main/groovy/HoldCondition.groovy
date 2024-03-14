import org.jetbrains.annotations.NotNull
import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.trade.Tool
import org.slf4j.Logger

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

class ScoreGroup extends LinkedHashMap<String, Scorable> implements Scorable {
    Number getAverage() {
        return this.values().collect{it.getAverage()}.average() as Number
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
    Scorable getTrailingStopScore()
    Scorable getUnderestimateScore()
    Scorable getOverestimateScore()
    Scorable getOversoldScore()
    Scorable getOverboughtScore()
}

class Analysis implements Analyzable {
    def log
    List<Ohlcv> ohlcvs
    Ohlcv ohlcv
    List<Ema> fastEmas
    Ema fastEma
    List<Ema> slowEmas
    Ema slowEma
    List<BollingerBand> bollingerBands
    BollingerBand bollingerBand
    List<Macd> macds
    Macd macd
    List<Rsi> rsis
    Rsi rsi
    List<Dmi> dmis
    Dmi dmi
    List<Cci> ccis
    Cci cci
    List<Atr> atrs
    Atr atr

    Analysis(List<Ohlcv> ohlcvs, log) {
        this.ohlcvs = ohlcvs
        this.ohlcv = this.ohlcvs.first()
        this.log = log
        this.fastEmas = Tool.calculate(ohlcvs, EmaContext.of(10))
        this.fastEma = this.fastEmas.first()
        this.slowEmas = Tool.calculate(ohlcvs, EmaContext.of(20))
        this.slowEma = this.slowEmas.first()
        this.bollingerBands = Tool.calculate(ohlcvs, BollingerBandContext.DEFAULT)
        this.bollingerBand = this.bollingerBands.first()
        this.macds = Tool.calculate(ohlcvs, MacdContext.DEFAULT)
        this.macd = this.macds.first()
        this.rsis = Tool.calculate(ohlcvs, RsiContext.DEFAULT)
        this.rsi = this.rsis.first()
        this.dmis = Tool.calculate(ohlcvs, DmiContext.DEFAULT)
        this.dmi = this.dmis.first()
        this.ccis = Tool.calculate(ohlcvs, CciContext.DEFAULT)
        this.cci = this.ccis.first()
        this.atrs = Tool.calculate(ohlcvs, AtrContext.DEFAULT)
        this.atr = this.atrs.first()
    }

    @Override
    Scorable getBullishScore() {
        def score = new Score()
        // ema
        score.emaValueFastOverSlow = fastEma.value > slowEma.value ? 100 : 0
        // bollinger band
        score.bollingerBandPriceOverValue = ohlcv.closePrice > bollingerBand.middle ? 100 : 0
        // macd
        score.macdValueOverSignal = macd.value > macd.signal ? 100 : 0
        score.macdOscillator  = macd.oscillator > 0 ? 100 : 0
        // rsi
        score.rsiValueOverSignal = rsi.value > rsi.signal ? 100 : 0
        // dmi
        score.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
        // cci
        score.cciValueOverSignal = cci.value > cci.signal ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getBearishScore() {
        def score = new Score()
        // ema
        score.emaValueFastOverSlow = fastEma.value < slowEma.value ? 100 : 0
        // bollinger band
        score.bollingerBandPriceOverValue = ohlcv.closePrice < bollingerBand.middle ? 100 : 0
        // macd
        score.macdValueOverSignal = macd.value < macd.signal ? 100 : 0
        score.macdOscillator  = macd.oscillator < 0 ? 100 : 0
        // rsi
        score.rsiValueOverSignal = rsi.value < rsi.signal ? 100 : 0
        // dmi
        score.dmiPdiOverMdi = dmi.pdi < dmi.mdi ? 100 : 0
        // cci
        score.cciValueOverSignal = cci.value < cci.signal ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getVolatilityScore() {
        def score = new Score()
        // dmi
        def dmi = this.dmis.first()
        score.dmiAdx25 = dmi.adx > 25 ? 100 : 0
        return score
    }

    @Override
    Scorable getMomentumScore() {
        def score = new Score()
        // ema
        score.emaValueFastOverSlow = fastEma.value > slowEma.value ? 100 : 0
        // bollinger band
        score.bollingerBandPriceOverValue = ohlcv.closePrice > bollingerBand.middle ? 100 : 0
        // macd
        score.macdValue = macd.value > 0 ? 100 : 0
        // rsi
        score.rsiValue = rsi.value > 50 ? 100 : 0
        // dmi
        score.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
        score.dmiAdx = dmi.adx > 25 && dmi.pdi > dmi.mdi ? 100 : 0
        // cci
        score.cciValue = cci.value > 0 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getTrailingStopScore() {
        def score = new Score()
        // ATR based trailing stop loss
        Ohlcv prevOhlcv = ohlcvs.get(1)
        Atr prevAtr = atrs.get(1)
        score.atrValue = ohlcv.closePrice < prevOhlcv.highPrice - (prevAtr.value * 2.0) ? 100 : 0
        return score
    }

    @Override
    Scorable getUnderestimateScore() {
        def score = new Score()
        // macd
        score.macdValue = macd.value < 0 ? 100 : 0
        // rsi
        score.rsiValue = rsi.value < 50 ? 100 : 0
        // cci
        score.cciValue = cci.value < 0 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOverestimateScore() {
        def score = new Score()
        // macd
        def macd = this.macds.first()
        score.macdValue = macd.value > 0 ? 100 : 0
        // rsi
        score.rsiValue = rsi.value > 50 ? 100 : 0
        // cci
        score.cciValue = cci.value > 0 ? 100 : 0
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
                momentumScore: "${this.getMomentumScore().getAverage()}",
                underestimateScore: "${this.getUnderestimateScore().getAverage()}",
                overestimateScore: "${this.getOverestimateScore().getAverage()}",
        ].toString()
    }
}

class AnalysisGroup extends LinkedHashMap<String,Analysis> implements Analyzable {

    AnalysisGroup(Map map) {
        super(map)
    }

    @Override
    Scorable getBullishScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getBullishScore())}
        return scoreGroup
    }

    @Override
    Scorable getBearishScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getBearishScore())}
        return scoreGroup
    }

    @Override
    Scorable getVolatilityScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getVolatilityScore())}
        return scoreGroup
    }

    @Override
    Scorable getMomentumScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getMomentumScore())}
        return scoreGroup
    }

    @Override
    Scorable getTrailingStopScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getTrailingStopScore())}
        return scoreGroup
    }

    @Override
    Scorable getUnderestimateScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getUnderestimateScore())}
        return scoreGroup
    }

    @Override
    Scorable getOverestimateScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getOverestimateScore())}
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
    String toString() {
        return [
                bullishDivergenceScore: "${this.getMomentumScore().getAverage()}",
                bearishDivergenceScore: "${this.getBearishScore().getAverage()}",
                volatilityScore: "${this.getVolatilityScore().getAverage()}",
                momentumScore: "${this.getMomentumScore().getAverage()}",
                underestimateScore: "${this.getUnderestimateScore().getAverage()}",
                overestimateScore: "${this.getOverestimateScore().getAverage()}",
                oversoldScore: "${this.getOversoldScore().getAverage()}",
                overboughtScore: "${this.getOverboughtScore().getAverage()}"
        ].toString() + super.toString()
    }
}

//=============================
// define
//=============================
// default
def hold = null
List<Ohlcv> ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def analysis = new Analysis(ohlcvs, log)
def priceZScore = Tool.zScore(ohlcvs.take(20).collect{it.closePrice})
// skip when z-score under 1 abs (for performance)
if (priceZScore.abs() < 1.0) {
    log.info("skip - priceZScore is {}", priceZScore)
    return null
}

// wave analysis
def waveAnalysis = new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 5), log)

// tide analysis
def tideAnalysis = new AnalysisGroup(
        hourly: new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 60), log),
        daily: new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.DAILY, 1), log)
)

//=============================
// logging
//=============================
log.info("priceZScore: {}", priceZScore)
log.info("waveAnalysis.momentum: {}", waveAnalysis.getMomentumScore())
log.info("waveAnalysis.stopLoss: {}", waveAnalysis.getTrailingStopScore())
log.info("waveAnalysis.underestimate: {}", waveAnalysis.getUnderestimateScore())
log.info("waveAnalysis.overestimate: {}", waveAnalysis.getOverestimateScore())

//=============================
// trade
//=============================
// buy
if (priceZScore > 1.0 && analysis.getBullishScore().getAverage() > 60) {
    // only volatility is positive
    if (waveAnalysis.getVolatilityScore().getAverage() > 60) {
        // wave is bullish
        if (waveAnalysis.getMomentumScore().getAverage() > 60 && waveAnalysis.getBullishScore().getAverage() > 60) {
            hold = 1
        }
        // wave is oversold
        if (waveAnalysis.getOversoldScore().getAverage() > 60) {
            hold = 1
        }
        // wave is underestimate
        if (waveAnalysis.getUnderestimateScore().getAverage() > 60) {
            hold = 1
        }
    }
}
// sell
if (priceZScore < -1.0 && analysis.getBearishScore().getAverage() > 60) {
    // only volatility is positive
    if (waveAnalysis.getVolatilityScore().getAverage() > 60) {
        // check wave trailing stop
        if (waveAnalysis.getTrailingStopScore().getAverage() > 60) {
            hold = 0
            // ignore when momentum is strong
            if (waveAnalysis.getMomentumScore().getAverage() > 60) {
                hold = null
            }
        }
        // momentum is week
        if (waveAnalysis.getMomentumScore().getAverage() < 40) {
            hold = 0
        }
    }
}

//=============================
// fallback
//=============================
// tide is bearish
if (tideAnalysis.getBearishScore().getAverage() > 60) {
    log.info("fallback - tideAnalysis.bearishScore: {}", tideAnalysis.getMomentumScore())
//    hold = 0
}
// tide trailing stop
if (tideAnalysis.getTrailingStopScore().getAverage() > 60) {
    log.info("fallback - tideAnalysis.stopLossScore: {}", tideAnalysis.getTrailingStopScore())
//    hold = 0
}

//=============================
// return
//=============================
return hold

