Boolean hold;
int maPeriod = 20;
int slopePeriod = 30;
double priceSlope = tool.slope(assetIndicator.getMinutePrices(), slopePeriod);
double emaSlope = tool.slope(assetIndicator.getMinuteEmas(maPeriod), slopePeriod);
double smaSlope = tool.slope(assetIndicator.getMinuteSmas(maPeriod), slopePeriod);
double macdOscillator = assetIndicator.getMinuteMacd(12, 26, 9).getOscillator();
double macdOscillatorSlope = tool.slope(assetIndicator.getMinuteMacds(12, 26, 9).collect { it.oscillator }, slopePeriod);
double macdOscillatorAverage = tool.average(assetIndicator.getMinuteMacds(12, 26, 9).collect { it.oscillator }, slopePeriod);
double rsi = assetIndicator.getMinuteRsi(14);
double rsiSlope = tool.slope(assetIndicator.getMinuteRsis(14), slopePeriod);
double rsiAverage = tool.average(assetIndicator.getMinuteRsis(14), slopePeriod);

log.info("priceSlope:{}, emaSlope:{}, smaSlope:{} , macdOscillator:{}, macdOscillatorSlope:{}, macdOscillatorAverage:{}, rsi:{}, rsiSlope:{}",
priceSlope, emaSlope, smaSlope, macdOscillator, macdOscillatorSlope, macdOscillatorAverage, rsi, rsiSlope);

// 보유 조건 - 가격,SMA,EMA,MACD 모두 상승중인 경우
if(emaSlope > 0
//&& smaSlope > 0
//&& macdOscillator > 0
//&& macdOscillatorAverage > 0
//&& macdOscillatorSlope > 0
) {
    hold = true;
}
// 그외 보유하지 않음
else {
    hold = false;
}

// 마지막 거래일 경우 모두 매도(보유하지 않음)
if(lastTrade) {
    hold = false;
}

// 결과 반환
return hold;
