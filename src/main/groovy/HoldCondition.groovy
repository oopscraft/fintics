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
    Scorable getVolatilityScore()
    Scorable getBullishScore()
    Scorable getBearishScore()
    Scorable getUnderestimateScore()
    Scorable getOverestimateScore()
    Scorable getOversoldScore()
    Scorable getOverboughtScore()
    Scorable getTrailingStopScore(multiplier)
}

class Analysis implements Analyzable {
    List<Ohlcv> ohlcvs
    Ohlcv ohlcv
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

    Analysis(List<Ohlcv> ohlcvs) {
        this.ohlcvs = ohlcvs
        this.ohlcv = this.ohlcvs.first()
        this.macds = Tool.calculate(ohlcvs, MacdContext.DEFAULT)
        this.macd = this.macds.first()
        this.rsis = Tool.calculate(ohlcvs, RsiContext.DEFAULT)
        this.rsi = this.rsis.first()
        this.dmis = Tool.calculate(ohlcvs, DmiContext.DEFAULT)
        this.dmi = this.dmis.first()
        this.atrs = Tool.calculate(ohlcvs, AtrContext.DEFAULT)
        this.atr = this.atrs.first()
        this.bollingerBands = Tool.calculate(ohlcvs, BollingerBandContext.DEFAULT)
        this.bollingerBand = bollingerBands.first()
    }

    @Override
    Scorable getVolatilityScore() {
        def score = new Score()
        // dmi
        score.dmiAdx20 = dmi.adx > 20 ? 100 : 0
        // bollinger band
        def averageBollingerBandWidth = bollingerBands.collect{it.width}.min()
        score.bollingerBandWidth = bollingerBand.width > averageBollingerBandWidth*2 ? 100 : 0
        // atr
        def averageAtr = atrs.collect{it.value}.min()
        score.atrValue = atr.value > averageAtr*2 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getBullishScore() {
        def score = new Score()
        // macd
        score.macdValueOverSignal = macd.value > macd.signal ? 100 : 0
        score.macdOscillator  = macd.oscillator > 0 ? 100 : 0
        // rsi
        score.rsiValueOverSignal = rsi.value > rsi.signal ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getBearishScore() {
        def score = new Score()
        // macd
        score.macdValueUnderSignal = macd.value < macd.signal ? 100 : 0
        score.macdOscillator  = macd.oscillator < 0 ? 100 : 0
        // rsi
        score.rsiValueUnderSignal = rsi.value < rsi.signal ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getUnderestimateScore() {
        def score = new Score()
        // macd
        score.macdValue = macd.value < 0 ? 100 : 0
        // rsi
        score.rsiValue = rsi.value < 50 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOverestimateScore() {
        def score = new Score()
        // macd
        score.macdValue = macd.value > 0 ? 100 : 0
        // rsi
        score.rsiValue = rsi.value > 50 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOversoldScore() {
        def score = new Score()
        // rsi
        def rsiOversoldExisted = rsis.drop(1).take(3).any{it.value <= 30}
        def rsiOversoldSolved = rsi.value > 30 && rsi.value > rsi.signal
        score.rsiValue = rsiOversoldExisted && rsiOversoldSolved ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOverboughtScore() {
        def score = new Score()
        // rsi
        def rsiOverboughtExisted = rsis.drop(1).take(3).any{it.value >= 70}
        def rsiOverboughtSolved = rsi.value < 70 && rsi.value < rsi.signal
        score.rsiValue = rsiOverboughtExisted && rsiOverboughtSolved ? 100 : 0
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
        return score
    }

    @Override
    String toString() {
        return [
                volatilityScore: "${this.getVolatilityScore()}",
                bullishScore: "${this.getBullishScore()}",
                bearishScore: "${this.getBearishScore()}",
                oversoldScore: "${this.getOversoldScore().getAverage()}",
                overboughtScore: "${this.getOverboughtScore().getAverage()}",
        ].toString()
    }
}

class AnalysisGroup extends LinkedHashMap<String,Analysis> implements Analyzable {

    AnalysisGroup(Map map) {
        super(map)
    }

    @Override
    Scorable getVolatilityScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getVolatilityScore())}
        return scoreGroup
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
    Scorable getTrailingStopScore(multiplier) {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getTrailingStopScore(multiplier))}
        return scoreGroup
    }

    @Override
    String toString() {
        return [
                volatilityScore: "${this.getVolatilityScore().getAverage()}",
                bullishScore: "${this.getMomentumScore().getAverage()}",
                bearishScore: "${this.getBearishScore().getAverage()}",
                oversoldScore: "${this.getOversoldScore().getAverage()}",
                overboughtScore: "${this.getOverboughtScore().getAverage()}",
        ].toString() + super.toString()
    }
}

//================================
// define
//================================
// default
def hold = null
List<Ohlcv> ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def priceZScores = Tool.zScores(ohlcvs.take(10).collect{it.closePrice})
def priceZScore = priceZScores.first()
def analysis = new Analysis(ohlcvs)
log.info("analysis: {}", analysis)

// wave
def waveAnalysis = new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 3))
log.info("waveAnalysis: {}", waveAnalysis)

// tide
def tideAnalysis = new AnalysisGroup (
        daily: new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.DAILY, 1))
)

//================================
// trade
//================================
if (priceZScore > 1.5 && analysis.getBullishScore().getAverage() > 70) {
    if (waveAnalysis.getVolatilityScore().getAverage() > 70) {
        // momentum
        if (waveAnalysis.getBullishScore().getAverage() > 70) {
            hold = 1
        }
        // oversold
        if (waveAnalysis.getOversoldScore().getAverage() > 70) {
            hold = 1
        }
    }
}
if (priceZScore < -1.5 & analysis.getBearishScore().getAverage() > 70) {
    if (waveAnalysis.getBearishScore().getAverage() > 70) {
        hold = 0
    }
}

//================================
// fallback
//================================
// if tide bearish, disable
if (tideAnalysis.getBearishScore().getAverage() > 70) {
    hold = 0
}

//================================
// return
//================================
return hold

