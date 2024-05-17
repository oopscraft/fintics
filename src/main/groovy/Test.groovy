import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.model.StrategyResult
import org.oopscraft.fintics.trade.Tools
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
        this.macds = Tools.indicators(ohlcvs, MacdContext.DEFAULT)
        this.macd = this.macds.first()
        this.dmis = Tools.indicators(ohlcvs, DmiContext.DEFAULT)
        this.dmi = this.dmis.first()
        this.rsis = Tools.indicators(ohlcvs, RsiContext.DEFAULT)
        this.rsi = rsis.first()
        this.atrs = Tools.indicators(ohlcvs, AtrContext.DEFAULT)
        this.atr = atrs.first()
        this.bollingerBands = Tools.indicators(ohlcvs, BollingerBandContext.DEFAULT)
        this.bollingerBand = bollingerBands.first()
        this.obvs = Tools.indicators(ohlcvs, ObvContext.DEFAULT)
        this.obv = obvs.first()
        this.chaikinOscillators = Tools.indicators(ohlcvs, ChaikinOscillatorContext.DEFAULT)
        this.chaikinOscillator = chaikinOscillators.first()
        this.ccis = Tools.indicators(ohlcvs, CciContext.DEFAULT)
        this.cci = ccis.first()
        this.stochasticSlows = Tools.indicators(ohlcvs, StochasticSlowContext.DEFAULT)
        this.stochasticSlow = stochasticSlows.first()
    }

    @Override
    Scorable getMomentumScore() {
        def score = new Score()
        // macd
        score.macdValueOverSignal = macd.value > macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
        score.macdValue = macd.value > 0 ? 100 : 0
        // rsi
        score.rsiValueOverSignal = rsi.value > rsi.signal ? 100 : 0
        score.rsiValue = rsi.value > 50 ? 100 : 0
        // cci
        score.cciValueOverSignal = cci.value > cci.signal ? 100 : 0
        score.cciValue = cci.value > 0 ? 100 : 0
        // dmi
        score.dmiPdiPctChange = Tools.pctChange(dmis.take(10).collect{it.pdi}) > 0 ? 100 : 0
        score.dmiMdiPctChange = Tools.pctChange(dmis.take(10).collect{it.mdi}) < 0 ? 100 : 0
        score.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
        // obv
        score.obvValueOverSignal = obv.value > obv.signal ? 100 : 0
        score.obvPctChange = Tools.pctChange(obvs.take(10).collect{it.value}) > 0 ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValueOverSignal = chaikinOscillator.value > chaikinOscillator.signal ? 100 : 0
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
                momentumScore: "${this.getMomentumScore()}",
                volatilityScore: "${this.getVolatilityScore()}",
                underestimateScore: "${this.getUnderestimateScore()}",
                overestimateScore: "${this.getOverestimateScore()}",
                overboughtScore: "${this.getOverboughtScore()}",
                oversoldScore: "${this.getOversoldScore()}"
        ].toString()
    }
}

//================================
// define
//================================
// default
StrategyResult strategyResult = null
List<Ohlcv> ohlcvs = assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, 1)

// analysis
def analysis = new Analysis(ohlcvs)

// logging
log.info("analysis.momentum: {} {}", analysis.getMomentumScore().getAverage(), analysis.getMomentumScore());
log.info("analysis.volatility: {} {}", analysis.getVolatilityScore().getAverage(), analysis.getVolatilityScore())

//================================
// trade
//================================
// buy
if (analysis.getMomentumScore().getAverage() > 75) {
    // default
    strategyResult = StrategyResult.of(1.0, "buy");
    // filter - volatility
    if (analysis.getVolatilityScore().getAverage() < 75) {
        strategyResult = null
    }
}
// sell
if (analysis.getMomentumScore().getAverage() < 25) {
    // default
    strategyResult = StrategyResult.of(0.0, "sell")
    // filter - volatility
    if (analysis.getVolatilityScore().getAverage() < 75) {
        strategyResult = null
    }
}

//================================
// return
//================================
return strategyResult
