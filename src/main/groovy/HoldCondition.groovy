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
    Scorable getBullishMomentumScore()
    Scorable getBearishMomentumScore()
    Scorable getVolatilityScore()
    Scorable getUnderEstimateScore()
    Scorable getOverEstimateScore()
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
    Scorable getBullishMomentumScore() {
        def score = new Score()
        // macd
        def macd = this.macds.first()
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getBearishMomentumScore() {
        def score = new Score()
        // macd
        def macd = this.macds.first()
        score.macdOscillator = macd.oscillator < 0 ? 100 : 0
        // return
        return score
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
    Scorable getUnderEstimateScore() {
        def score = new Score()
        // rsi
        def rsi = this.rsis.first()
        score.rsiValue = rsi.value <= 30 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOverEstimateScore() {
        def score = new Score()
        // rsi
        def rsi = this.rsis.first()
        score.rsiValue = rsi.value >= 70 ? 100 : 0
        // return
        return score
    }

    @Override
    String toString() {
        return [bullishMomentumScore: "${this.getBullishMomentumScore().getAverage()}",
                bearishMomentumScore: "${this.getBearishMomentumScore().getAverage()}",
                volatilityScore: "${this.getVolatilityScore().getAverage()}",
                underEstimateScore: "${this.getUnderEstimateScore().getAverage()}",
                overEstimateScore: "${this.getOverEstimateScore().getAverage()}"
        ].toString()
    }
}

class AnalysisGroup extends LinkedHashMap<String,Analysis> implements Analyzable {

    AnalysisGroup(Map map) {
        super(map)
    }

    @Override
    Scorable getBullishMomentumScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getBullishMomentumScore())}
        return scoreGroup
    }

    @Override
    Scorable getBearishMomentumScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getBearishMomentumScore())}
        return scoreGroup
    }

    @Override
    Scorable getVolatilityScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getVolatilityScore())}
        return scoreGroup
    }

    @Override
    Scorable getUnderEstimateScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getUnderEstimateScore())}
        return scoreGroup
    }

    @Override
    Scorable getOverEstimateScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getOverEstimateScore())}
        return scoreGroup
    }

    @Override
    String toString() {
        return [bullishMomentumScore: "${this.getBullishMomentumScore().getAverage()}",
                bearishMomentumScore: "${this.getBearishMomentumScore().getAverage()}",
                volatilityScore: "${this.getVolatilityScore().getAverage()}",
                underEstimateScore: "${this.getUnderEstimateScore().getAverage()}",
                overEstimateScore: "${this.getOverEstimateScore().getAverage()}"
        ].toString() + super.toString()
    }
}

// define
def hold = null
List<Ohlcv> ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def analysis = new Analysis(ohlcvs)

// wave analysis
def waveAnalysis = new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 5))

// tide analysis
def tideAnalysis = new AnalysisGroup(
        daily: new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.DAILY,1))
)

// logging
log.info("analysis.bullishMomentum: {}", analysis.getBullishMomentumScore())
log.info("analysis.bearishMomentum: {}", analysis.getBearishMomentumScore())
log.info("waveAnalysis.volatility: {}", waveAnalysis.getVolatilityScore())
log.info("waveAnalysis.bullishMomentum: {}", waveAnalysis.getBullishMomentumScore())
log.info("waveAnalysis.bearishMomentum: {}", waveAnalysis.getBearishMomentumScore())
log.info("waveAnalysis.underEstimate: {}", waveAnalysis.getUnderEstimateScore())
log.info("waveAnalysis.overEstimate: {}", waveAnalysis.getOverEstimateScore())

// move up
if (analysis.getBullishMomentumScore().getAverage() > 70) {
//    if (waveAnalysis.getVolatilityScore().getAverage() > 70) {
//        if (waveAnalysis.getBullishMomentumScore().getAverage() > 70) {
//            if (waveAnalysis.getUnderEstimateScore().getAverage() > 70) {
                hold = 1
//            }
//        }
//    }
}

// move down
if (analysis.getBearishMomentumScore().getAverage() > 70) {
//    if (waveAnalysis.getVolatilityScore().getAverage() > 70) {
//        if (waveAnalysis.getBearishMomentumScore().getAverage() > 70) {
//            if (waveAnalysis.getOverEstimateScore() > 70) {
                hold = 0
//            }
//        }
//    }
}

//// fallback
//if (tideAnalysis.getMomentumScore().getAverage() < 30) {
//    //hold = 0
//}
//if (tideAnalysis.getCircuitBreakerScore().getAverage() > 70) {
//    //hold = 0
//}

// return
return hold

