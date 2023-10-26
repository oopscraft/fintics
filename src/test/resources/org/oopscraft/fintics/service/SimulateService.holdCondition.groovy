import java.time.LocalDateTime

Boolean hold;
LocalDateTime dateTime = assetIndicator.getMinuteDateTimes().get(0);
double price = assetIndicator.getMinutePrices().get(0);
double emaSlope = tool.slope(assetIndicator.getMinuteEmas(20), 20);
double macdOscillatorAverage = tool.average(assetIndicator.getMinuteMacds(24, 52, 18).collect { it.oscillator }, 20);
double rsiAverage = tool.average(assetIndicator.getMinuteRsis(28), 20);

log.info("- dateTime:{}, price:{}, emaSlope:{}, macdOscillatorAverage:{}, rsiAverage:{}",
dateTime, price, emaSlope, macdOscillatorAverage, rsiAverage);

// 매수조건
if(emaSlope > 0) {
    if(macdOscillatorAverage > 0 && rsiAverage > 50) {
        hold = true;
    }
}

// 매도조건
if(emaSlope < 0) {
    if(macdOscillatorAverage < 0 && rsiAverage < 50) {
        hold = false;
    }
}

// 마지막 거래일 경우 모두 매도(보유하지 않음)
if(lastTrade) {
    hold = false;
}

// 결과 반환
return hold;
