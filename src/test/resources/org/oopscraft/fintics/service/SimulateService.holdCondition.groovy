import java.time.LocalDateTime

Boolean hold;
LocalDateTime dateTime = assetIndicator.getMinuteDateTimes().get(0);
double price = assetIndicator.getMinutePrices().get(0);
double priceSlope = tool.slope(assetIndicator.getMinutePrices(), 3);
double emaSlope = tool.slope(assetIndicator.getMinuteEmas(20), 3);
double macdOscillator = assetIndicator.getMinuteMacd(24, 52, 18).getOscillator();
double rsi = assetIndicator.getMinuteRsi(14);

log.info("- dateTime:{}, price:{}, priceSlope:{}, emaSlope:{}, macdOscillator:{}, rsi:{}",
dateTime, price, priceSlope, emaSlope, macdOscillator, rsi);

// 보유조건
if(emaSlope > 0 && macdOscillator > 0) {
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
