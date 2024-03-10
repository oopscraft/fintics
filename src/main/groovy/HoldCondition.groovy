import org.jetbrains.annotations.NotNull
import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.trade.Tool

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
    BigDecimal getPriceZScore()
    Scorable getVolatilityScore()
    Scorable getMomentumScore()
    Scorable getPostOversoldScore()
    Scorable getPostOverboughtScore()
}

class Analysis implements Analyzable {
    List<Ohlcv> ohlcvs
    List<Dmi> dmis
    List<Macd> macds
    List<Rsi> rsis

    Analysis(List<Ohlcv> ohlcvs) {
        this.ohlcvs = ohlcvs
        this.dmis = Tool.calculate(ohlcvs, DmiContext.DEFAULT)
        this.macds = Tool.calculate(ohlcvs, MacdContext.DEFAULT)
        this.rsis = Tool.calculate(ohlcvs, RsiContext.DEFAULT)
    }

    @Override
    BigDecimal getPriceZScore() {
        def closePrices = ohlcvs.take(20).collect{it.closePrice}
        def zScores = Tool.zScores(closePrices)
        return zScores.first()
    }

    @Override
    Scorable getVolatilityScore() {
        def score = new Score()
        // dmi
        def dmi = this.dmis.first()
        score.dmiAdx = dmi.adx > 25 ? 100 : 0
        return score
    }

    @Override
    Scorable getMomentumScore() {
        def score = new Score()
        // macd
        def macd = this.macds.first()
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getPostOversoldScore() {
        def score = new Score()
        // rsi
        def rsi = this.rsis.first()
        score.rsiValue = rsi.value <= 30 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getPostOverboughtScore() {
        def score = new Score()
        // rsi
        def rsi = this.rsis.first()
        score.rsiValue = rsi.value >= 70 ? 100 : 0
        // return
        return score
    }

    @Override
    String toString() {
        return [momentumScore: "${this.getMomentumScore().getAverage()}",
                oversoldScore: "${this.getPostOversoldScore().getAverage()}",
                overboughtScore: "${this.getPostOverboughtScore().getAverage()}"
        ].toString()
    }
}

class AnalysisGroup extends LinkedHashMap<String,Analysis> implements Analyzable {

    AnalysisGroup(Map map) {
        super(map)
    }

    @Override
    BigDecimal getPriceZScore() {
        return this.values().collect{it.getPriceZScore()}.average() as BigDecimal
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
    Scorable getPostOversoldScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getPostOversoldScore())}
        return scoreGroup
    }

    @Override
    Scorable getPostOverboughtScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getPostOverboughtScore())}
        return scoreGroup
    }

    @Override
    String toString() {
        return [momentumScore: "${this.getMomentumScore().getAverage()}",
                oversoldScore: "${this.getPostOversoldScore().getAverage()}",
                overboughtScore: "${this.getPostOverboughtScore().getAverage()}"
        ].toString() + super.toString()
    }
}

// define
def hold = null
List<Ohlcv> ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def analysis = new Analysis(ohlcvs)

// wave analysis
def waveAnalysis = new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 5))
log.info("waveAnalysis.volatilityScore: {}", waveAnalysis.getVolatilityScore())
log.info("waveAnalysis.momentumScore: {}", waveAnalysis.getMomentumScore())
log.info("waveAnalysis.postOversoldScore: {}", waveAnalysis.getPostOversoldScore())
log.info("waveAnalysis.postOverboughtScore: {}", waveAnalysis.getPostOverboughtScore())

// tide analysis - not reference
def tideAnalysis = new AnalysisGroup(
        daily: new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.DAILY,1))
)

// move up
if (analysis.getPriceZScore() >= 2.0) {
    if (waveAnalysis.getVolatilityScore().getAverage() > 70) {
        if (waveAnalysis.getMomentumScore().getAverage() > 70) {
            hold = 1
        }
        if (waveAnalysis.getPostOversoldScore().getAverage() > 70) {
            hold = 1
        }
    }
}

// move down
if (analysis.getPriceZScore() <= -2.0) {
    if (waveAnalysis.getVolatilityScore().getAverage() > 70) {
        if (waveAnalysis.getMomentumScore().getAverage() < 30) {
            hold = 0
        }
        if (waveAnalysis.getPostOverboughtScore().getAverage() > 70) {
            hold = 0
        }
    }
}

// fallback
if (tideAnalysis.getMomentumScore().getAverage() < 30) {
//    hold = 0
}

// return
return hold

