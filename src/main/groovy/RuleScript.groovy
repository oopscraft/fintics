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
    BigDecimal getZScore()
    Scorable getBullishScore()
    Scorable getBearishScore()
    Scorable getVolatilityScore()
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

    Analysis(List<Ohlcv> ohlcvs) {
        this.ohlcvs = ohlcvs
        this.ohlcv = this.ohlcvs.first()
        this.macds = Tool.calculate(ohlcvs, MacdContext.DEFAULT)
        this.macd = this.macds.first()
        this.dmis = Tool.calculate(ohlcvs, DmiContext.DEFAULT)
        this.dmi = this.dmis.first()
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
        // return
        return score
    }

    @Override
    Scorable getBearishScore() {
        def score = new Score()
        // macd
        score.macdValueUnderSignal = macd.value < macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator < 0 ? 100 : 0
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
    String toString() {
        return [
                bullishScore: "${this.getBullishScore()}",
                bearishScore: "${this.getBearishScore()}",
                volatilityScore: "${this.getVolatilityScore()}",
                underestimateScore: "${this.getUnderestimateScore()}",
                overestimateScore: "${this.getOverestimateScore()}"
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

// wave
def waveAnalysis = new Analysis(assetIndicator.getOhlcvs(waveOhlcvType, waveOhlcvPeriod))
log.info("waveAnalysis: {}", waveAnalysis)

// tide
def tideAnalysis = new Analysis(assetIndicator.getOhlcvs(tideOhlcvType, tideOhlcvPeriod))
log.info("tideAnalysis: {}", tideAnalysis)

//================================
// trade
//================================
// buy
if (waveAnalysis.getZScore() > 1.5 && waveAnalysis.getBullishScore().getAverage() > 70) {
    if (waveAnalysis.getVolatilityScore().getAverage() > 70) {
        if (waveAnalysis.getUnderestimateScore().getAverage() > 70) {
            hold = 1
        }
    }
}
// sell
if (waveAnalysis.getZScore() < -1.5 && waveAnalysis.getBearishScore().getAverage() > 70) {
    if (waveAnalysis.getVolatilityScore().getAverage() > 70) {
        if (waveAnalysis.getOverestimateScore().getAverage() > 70) {
            hold = 0
        }
    }
}

//================================
// fallback
//================================
// tide is bearish, disable
if (tideAnalysis.getBearishScore().getAverage() > 70) {
    hold= 0
}

//================================
// return
//================================
return hold

