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

    Analysis(List<Ohlcv> ohlcvs) {
        this.ohlcvs = ohlcvs
        this.ohlcv = this.ohlcvs.first()
        this.macds = Tool.indicators(ohlcvs, MacdContext.DEFAULT)
        this.macd = this.macds.first()
        this.dmis = Tool.indicators(ohlcvs, DmiContext.DEFAULT)
        this.dmi = this.dmis.first()
        this.rsis = Tool.indicators(ohlcvs, RsiContext.DEFAULT)
        this.rsi = rsis.first()
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
        // return
        return score
    }

    @Override
    Scorable getBearishScore() {
        def score = new Score()
        // macd
        score.macdValueUnderSignal = macd.value < macd.signal ? 100 : 0
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
if (analysis.getZScore() > 1.0 && analysis.getBullishScore().getAverage() > 75) {
    if (waveAnalysis.getBullishScore().getAverage() > 75) {
        // default
        hold = 1
        // volatility
        if (waveAnalysis.getVolatilityScore().getAverage() < 75) {
            hold = null
        }
        // overestimate
        if (waveAnalysis.getOverestimateScore().getAverage() > 75) {
            hold = null
        }
    }
}
// sell
if (analysis.getZScore() < -1.0 && analysis.getBearishScore().getAverage() > 75) {
    if (waveAnalysis.getBearishScore().getAverage() > 75) {
        // default
        hold = 0
        // momentum
        if (waveAnalysis.getMomentumScore().getAverage() > 75) {
            hold = null
        }
    }
}

//================================
// fallback
//================================
// tide is bearish, disable
if (tideAnalysis.getBearishScore().getAverage() > 75) {
    hold = 0
}

//================================
// return
//================================
return hold
