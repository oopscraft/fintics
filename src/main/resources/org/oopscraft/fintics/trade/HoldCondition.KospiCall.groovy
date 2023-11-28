import java.time.LocalTime
import org.oopscraft.fintics.calculator.AdContext
import org.oopscraft.fintics.calculator.DmiContext
import org.oopscraft.fintics.calculator.ObvContext
import org.oopscraft.fintics.calculator.WvadContext
import org.oopscraft.fintics.model.Indicator
import org.oopscraft.fintics.model.OhlcvType
import org.oopscraft.fintics.calculator.MacdContext
import org.oopscraft.fintics.calculator.RsiContext
import org.oopscraft.fintics.calculator.SmaContext


def analyze(Indicator indicator, OhlcvType ohlcvType, int period) {
    // info
    def name = indicator.getName() + ':' + ohlcvType + ':' + period;

    // price, volume
    def ohlcvs = indicator.resample(ohlcvType, period);
    def ohlcv = ohlcvs.first();
    def prices = ohlcvs.collect{it.closePrice};
    def price = prices.first();
    def pricePctChange = tool.sum(tool.pctChanges(prices).take(3));
    def volumes = ohlcvs.collect{it.volume};
    def volume = volumes.first();
    def volumePctChange = tool.sum(tool.pctChanges(volumes).take(3));
    log.debug("[{}] ohlcv: {}", name, ohlcv);
    log.debug("[{}] price: {}({}%)", name, price, pricePctChange);
    log.debug("[{}] volume: {}({}%)", name, volume, volumePctChange);
    log.debug("[{}] {}", name, tool.graph("Prices", prices));
    log.debug("[{}] {}", name, tool.graph("Volumes", volumes));

    // priceMa
    def priceMas = indicator.calculate(ohlcvType, period, SmaContext.DEFAULT);
    def priceMa = priceMas.first();
    def priceMaValues = priceMas.collect{it.value};
    def priceMaValue = priceMaValues.first();
    def priceMaValuePctChange = tool.sum(tool.pctChanges(priceMaValues).take(3));
    log.debug("[{}] priceMa: {}", name, priceMa);
    log.debug("[{}] priceMaValue: {}({}%)", name, priceMaValue, priceMaValuePctChange);
    log.debug("[{}] {}", name, tool.graph("PriceMaValues", priceMaValues));

    // macd
    def macds = indicator.calculate(ohlcvType, period, MacdContext.DEFAULT);
    def macd = macds.first();
    def macdValues = macds.collect{it.value};
    def macdValue = macdValues.first();
    def macdValuePctChange = tool.mean(tool.pctChanges(macdValues).take(3));
    def macdOscillators = macds.collect{it.oscillator};
    def macdOscillator = macdOscillators.first();
    def macdOscillatorPctChange = tool.sum(tool.pctChanges(macdOscillators).take(3));
    log.debug("[{}] macd: {}", name, macd);
    log.debug("[{}] macdValue: {}({}%)", name, macdValue, macdValuePctChange);
    log.debug("[{}] macdOscillator: {}({}%)", name, macdOscillator, macdOscillatorPctChange);
    log.debug("[{}] {}", name, tool.graph("MacdValues", macdValues));

    // rsi
    def rsis = indicator.calculate(ohlcvType, period, RsiContext.DEFAULT);
    def rsi = rsis.first();
    def rsiValues = rsis.collect{it.value};
    def rsiValue = rsiValues.first();
    def rsiValuePctChange = tool.sum(tool.pctChanges(rsiValues).take(3));
    log.debug("[{}] rsi: {}", name, rsi);
    log.debug("[{}] rsiValue: {}({}%)", name, rsiValue, rsiValuePctChange);
    log.debug("[{}] {}", name, tool.graph("RsiValues", rsiValues));

    // dmi
    def dmis = indicator.calculate(ohlcvType, period, DmiContext.DEFAULT);
    def dmi = dmis.first();
    def dmiPdis = dmis.collect{it.pdi};
    def dmiPdi = dmiPdis.first();
    def dmiPdiPctChange = tool.sum(tool.pctChanges(dmiPdis).take(3));
    def dmiMdis = dmis.collect{it.mdi};
    def dmiMdi = dmiMdis.first();
    def dmiMdiPctChange = tool.sum(tool.pctChanges(dmiMdis).take(3));
    def dmiAdxs = dmis.collect{it.adx};
    def dmiAdx = dmiAdxs.first();
    def dmiAdxPctChange = tool.sum(tool.pctChanges(dmiAdxs).take(3));
    log.debug("[{}] dmi: {}", name, dmi);
    log.debug("[{}] dmiPdiValue: {}({}%)", name, dmiPdi, dmiPdiPctChange);
    log.debug("[{}] dmiMidValue: {}({}%)", name, dmiMdi, dmiMdiPctChange);
    log.debug("[{}] dmiAdxValue: {}({}%)", name, dmiAdx, dmiAdxPctChange);
    log.debug("[{}] {}", name, tool.graph("DMI Pdi", dmiPdis));
    log.debug("[{}] {}", name, tool.graph("DMI Mdi", dmiMdis));

    // obv
    def obvs = indicator.calculate(ohlcvType, period, ObvContext.DEFAULT);
    def obv = obvs.first();
    def obvValues = obvs.collect{it.value};
    def obvValue = obvValues.first();
    def obvValuePctChange = tool.sum(tool.pctChanges(obvValues).take(3));
    log.debug("[{}] obv:{}", name, obv);
    log.debug("[{}] obvValue: {}({}%)", name, obvValue, obvValuePctChange);
    log.debug("[{}] {}", name, tool.graph("OBV Values", obvValues));

    // ad
    def ads = indicator.calculate(ohlcvType, period, AdContext.DEFAULT);
    def ad = ads.first();
    def adValues = ads.collect{it.value};
    def adValue = adValues.first();
    def adValuePctChange = tool.sum(tool.pctChanges(adValues).take(3));
    log.debug("[{}] ad: {}", name, ad);
    log.debug("[{}] adValue: {}({}%)", name, adValue, adValuePctChange);
    log.debug("[{}] {}", name, tool.graph("AD Values", adValues));

    // wvad
    def wvads = indicator.calculate(ohlcvType, period, WvadContext.DEFAULT);
    def wvad = wvads.first();
    def wvadValues = wvads.collect{it.value};
    def wvadValue = wvads.first();
    def wvadValuePctChange = tool.sum(tool.pctChanges(wvadValues).take(3));
    log.debug("[{}] wvad: {}", name, wvad);
    log.debug("[{}] wvadValue: {}({}%)", name, wvadValue, wvadValuePctChange);
    log.debug("[{}] {}", name, tool.graph("WVAD Values", wvadValues));

    // result
    def result = [:];
    result.priceUp = (pricePctChange > 0.0 ? 100 : 0);
    result.priceVolumeUp = (pricePctChange > 0.0 && volumePctChange > 0.0 ? 100 : 0);
    result.priceOverPriceMa = (price > priceMaValue ? 100 : 0);
    result.priceMaUp = (priceMaValuePctChange > 0.0 ? 100 : 0);
    result.macdValue = (macdValue > 0 ? 100 : 0);
    result.macdValueUp = (macdValuePctChange > 0.0 ? 100 : 0);
    result.macdOscillator = (macdOscillator > 0 ? 100 : 0);
    result.macdOscillatorPctChange = (macdOscillatorPctChange > 0.0 ? 100 : 0);
    result.rsiOver50 = (rsiValue > 50 ? 100 : 0);
    result.rsiUp = (rsiValuePctChange > 0.0 ? 100 :0);
    result.dmiPdiOverMdi = (dmiPdi > dmiMdi ? 100 : 0);
    result.dmiPdiUp = (dmiPdiPctChange > 0.0 ? 100 : 0);
    result.dmiMdiDown = (dmiMdiPctChange < 0.0 ? 100 : 0);
    result.dmiPdiAdxUp = (dmiPdiPctChange > 0.0 && dmiAdxPctChange > 0.0 ? 100 : 0);
    result.obvValueUp = (obvValuePctChange > 0.0 ? 100 : 0);
    result.adValueUp = (adValuePctChange > 0.0 ? 100 : 0);
    result.wvadValueUp = (wvadValuePctChange > 0.0 ? 100 : 0);

    // return
    return result;
}

// defines
def assetName = tradeAssetIndicator.getName();
def holdVotes = [];

// minute 1
def resultOfMinute1 = analyze(tradeAssetIndicator, OhlcvType.MINUTE, 1);
holdVotes.addAll(resultOfMinute1.values());
log.debug("[{}] resultOfMinute1: {}", assetName, resultOfMinute1);
log.info("[{}] resultOfMinute1Average: {}", assetName, resultOfMinute1.values().average());

// minute 5
def resultOfMinute5 = analyze(tradeAssetIndicator, OhlcvType.MINUTE, 5);
holdVotes.addAll(resultOfMinute5.values());
log.debug("[{}] resultOfMinute5: {}", assetName, resultOfMinute5);
log.info("[{}] resultOfMinute5Average: {}", assetName, resultOfMinute5.values().average());

// minute 10
def resultOfMinute10 = analyze(tradeAssetIndicator, OhlcvType.MINUTE, 10);
holdVotes.addAll(resultOfMinute10.values());
log.debug("[{}] resultOfMinute10: {}", assetName, resultOfMinute10);
log.info("[{}] resultOfMinute10Average: {}", assetName, resultOfMinute10.values().average());

// minute 15
def resultOfMinute15 = analyze(tradeAssetIndicator, OhlcvType.MINUTE, 15);
holdVotes.addAll(resultOfMinute15.values());
log.debug("[{}] resultOfMinute15: {}", assetName, resultOfMinute15);
log.info("[{}] resultOfMinute15Average: {}", assetName, resultOfMinute15.values().average());

// minute 30
def resultOfMinute30 = analyze(tradeAssetIndicator, OhlcvType.MINUTE, 30);
holdVotes.addAll(resultOfMinute30.values());
log.debug("[{}] resultOfMinute30: {}", assetName, resultOfMinute30);
log.info("[{}] resultOfMinute30Average: {}", assetName, resultOfMinute30.values().average());

// Kospi minute 10 (코스피 지수 상승 시 매수)
def resultOfKospiMinute10 = analyze(indiceIndicators['KOSPI'], OhlcvType.MINUTE, 10);
holdVotes.addAll(resultOfKospiMinute10.values());
log.debug("[{}] resultOfKospiMinute10: {}", assetName, resultOfKospiMinute10);
log.info("[{}] resultOfKospiMinute10Average: {}", assetName, resultOfKospiMinute10.values().average());

// USD/KRW minute 10 (환율 하락 시 매수)
def resultOfUsdKrwMinute10 = analyze(indiceIndicators['USD_KRW'], OhlcvType.MINUTE, 10);
holdVotes.addAll(resultOfUsdKrwMinute10.values().collect{100 - (it as Number)});
log.debug("[{}] resultOfUsdKrwMinute10: {}", assetName, resultOfUsdKrwMinute10);
log.info("[{}] resultOfUsdKrwMinute10Average: {}", assetName, resultOfUsdKrwMinute10.values().average());

// Nasdaq Future minute 10 (나스닥 선물 상승 시 매수)
def resultOfNdxFutureMinute10 = analyze(indiceIndicators['NDX_FUTURE'], OhlcvType.MINUTE, 10);
holdVotes.addAll(resultOfNdxFutureMinute10.values());
log.debug("[{}] resultOfNdxFutureMinute10: {}", assetName, resultOfNdxFutureMinute10);
log.info("[{}] resultOfNdxFutureMinute10Average: {}", assetName, resultOfNdxFutureMinute10.values().average());

// 장시작 후 초반 추가 Factor (10:00 이전)
if(dateTime.toLocalTime().isBefore(LocalTime.of(10,0))) {

    // 야간 미국 Nasdaq Factor 추가
    def resultOfNdxMinute10 = analyze(indiceIndicators['NDX'], OhlcvType.MINUTE, 10);
    holdVotes.addAll(resultOfNdxMinute10.values());
    log.debug("[{}] resultOfNdxMinute10: {}", assetName, resultOfNdxMinute10);
    log.info("[{}] resultOfNdxMinute10Average: {}", assetName, resultOfNdxMinute10.values().average());
}

// 장종료 전 추가 Factor (15:00 이후)
if(dateTime.toLocalTime().isAfter(LocalTime.of(15, 00))) {

    // Asset Daily Factor 추가
    def resultOfDaily1 = analyze(tradeAssetIndicator, OhlcvType.DAILY, 1);
    holdVotes.addAll(resultOfDaily1.values());
    log.debug("[{}] resultOfDaily1: {}", assetName, resultOfDaily1);
    log.info("[{}] resultOfDaily1Average: {}", assetName, resultOfDaily1.values().average());

    // Nasdaq Daily Factor 추가
    def resultOfNdxDaily1 = analyze(indiceIndicators['NDX'], OhlcvType.DAILY,  1);
    holdVotes.addAll(resultOfNdxDaily1.values());
    log.debug("[{}] resultOfNdxDaily1: {}", assetName, resultOfNdxDaily1);
    log.info("[{}] resultOfNdxDaily1Average: {}", assetName, resultOfNdxDaily1.values().average());
}

// decide hold
def hold = null;
def holdVotesAverage = holdVotes.average();
log.debug("[{}] holdVotes: {}", assetName, holdVotes);
log.info("[{}] holdVotesAverage: {}", assetName, holdVotesAverage);

// buy
if(holdVotesAverage > 70) {
    hold = true;
}

// sell
if(holdVotesAverage < 50) {
    hold = false;
}

// return
return hold;
