-- delete not used data
delete from `core_menu_i18n`;
delete from `core_menu`;
delete from `core_git`;
delete from `core_alarm`;

-- core_authority
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('TRADE','Y','Trade Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('TRADE_EDIT','Y','Trade Edit Authority');

-- core_role_authority
insert into `core_role_authority` (`role_id`,`authority_id`) values ('USER', 'TRADE');

-- core_menu
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('trade','Y',null,'Trade','/trade',1,'/static/image/icon-trade.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('trade','ko','트레이드');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`target`,`sort`,`icon`) values
    ('admin','N',null,'Admin','/admin','_blank',99,'/static/image/icon-admin.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('admin','ko','관리자');

-- core_alarm
insert into `core_alarm`
    (`alarm_id`,`alarm_name`,`client_type`,`client_config`) values
    ('fintics','Slack-Fintics','org.oopscraft.arch4j.core.alarm.client.slack.SlackAlarmClient','url=https://hooks.slack.com/services/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');

-- fintics_trade: 한국투자증권 모의투자 - 지수ETF
insert into `fintics_trade`
    (`trade_id`,`name`,`enabled`,`interval`,`start_at`,`end_at`,`client_type`,`client_properties`,`alarm_id`,`hold_condition`) values
    ('06c228451ce0400fa57bb36f0568d7cb','한국투자증권 모의투자 - 지수ETF','N','60','09:00','15:30',
     'org.oopscraft.fintics.client.kis.KisClient','production=false
apiUrl=https://openapivts.koreainvestment.com:29443
appKey=ENC(mqcEUDQAO57SaLBCvhoz0RpFYBXtMQG2Y+NIK2jfQZ6koEUDlYx+5W8AW+eW0KVd)
appSecret=ENC(cPLNx3yraMH7FKXfEkcs0/r7ZrKDrW7nBgQ/NpKs3BeQGUkjDl+j8VG0FOhqly/DYwJdyZ5kpLMJ7GAGSv8vEZhUOlSPPP1lFNiVyWZKk+b9jhzyCQOcKs5c+Q5BxHI/A4Nhf4oVIEm8N8nQzVAypLIBQZHu3Re+aPRkWUbdArWaBI+RyLSlemy2ZsDCJh6+/kh8Bic1ooCit0Jx4y1mBZFohtf4zRGdafkkIDIL1OujMz5yRDqigYSNvcSAXRUX)
accountNo=ENC(BCcz1quNQASvnQ4/ME2CSOiufCl6GfxN)
',null,'
import java.time.LocalTime;

Boolean hold;
int period = 10;

// OHLCV
def minuteOhlcv = assetIndicator.getMinuteOhlcv();
def dailyOhlcv = assetIndicator.getDailyOhlcv();
log.info("minuteOhlcv:{}", minuteOhlcv);
log.info("dailyOhlcv:{}", dailyOhlcv);

// MA indicator
def priceEma = assetIndicator.getMinuteEma(60);
def priceEmaSlope = tool.slope(assetIndicator.getMinuteEmas(60), period);
def priceSma = assetIndicator.getMinuteSma(60);
def priceSmaSlope = tool.slope(assetIndicator.getMinuteSmas(60), period);

// MACD indicator
def macds = assetIndicator.getMinuteMacds(60, 120, 40);
def macdOscillatorSlope = tool.slope(macds.collect { it.oscillator }, period);
def macdOscillatorAverage = tool.average(macds.collect { it.oscillator }, period);

// RSI indicator
def rsis = assetIndicator.getMinuteRsis(60);
def rsiSlope = tool.slope(rsis, period);
def rsiAverage = tool.average(rsis, period);

// DMI indicator
def dmis = assetIndicator.getMinuteDmis(60);
def dmiPdiAverage = tool.average(dmis.collect { it.pdi }, period);
def dmiPdiSlope = tool.slope(dmis.collect { it.pdi }, period);
def dmiMdiAverage = tool.average(dmis.collect { it.mdi }, period);
def dmiMdiSlope = tool.slope(dmis.collect { it.mdi }, period);

// daily
def dailyPeriod = 3;
def dailyMacds = assetIndicator.getDailyMacds(12, 16, 9);
def dailyMacdOscillatorAverage = tool.average(dailyMacds.collect { it.oscillator }, dailyPeriod);
def dailyMacdOscillatorSlope = tool.slope(dailyMacds.collect { it.oscillator }, dailyPeriod);
def dailyRsis = assetIndicator.getDailyRsis(14);
def dailyRsiAverage = tool.average(dailyRsis, dailyPeriod);
def dailyRsiSlope = tool.slope(dailyRsis, dailyPeriod);
def dailyDmis = assetIndicator.getDailyDmis(14);
def dailyDmiPdiAverage = tool.average(dailyDmis.collect { it.pdi }, period);
def dailyDmiPdiSlope = tool.slope(dailyDmis.collect { it.pdi }, period);
def dailyDmiMdiAverage = tool.average(dailyDmis.collect { it.mdi }, period);
def dailyDmiMdiSlope = tool.slope(dailyDmis.collect { it.mdi }, period);

// market
def spxFutureIndicator = market.getSpxFutureIndicator();
def spxFutureEmaSlope = tool.slope(spxFutureIndicator.getMinuteEmas(60), period);
def spxFutureMacds = spxFutureIndicator.getMinuteMacds(60, 120, 40);
def spxFutureMacdOscillatorSlope = tool.slope(spxFutureMacds.collect { it.oscillator }, period);
def spxFutureMacdOscillatorAverage = tool.average(spxFutureMacds.collect { it.oscillator }, period);
def spxFutureRsis = spxFutureIndicator.getMinuteRsis(60);
def spxFutureRsiSlope = tool.slope(spxFutureRsis, period);
def spxFutureRsiAverage = tool.average(spxFutureRsis, period);

// 매수조건
if(priceEmaSlope > 0) {
    def buyVotes = [];
    def weight = 100;
    buyVotes.add(macdOscillatorAverage > 0 ? weight : 0);
    buyVotes.add(macdOscillatorSlope > 0 ? weight : 0);
    buyVotes.add(rsiAverage > 50 ? weight : 0);
    buyVotes.add(rsiSlope > 0 ? weight : 0);
    buyVotes.add(dmiPdiAverage > dmiMdiAverage ? weight : 0);
    buyVotes.add(dmiPdiSlope > 0 ? weight : 0);
    buyVotes.add(dmiMdiSlope < 0 ? weight : 0);
    // daily factor
    def dailyWeight = 100;
    buyVotes.add(dailyMacdOscillatorAverage > 0 ? dailyWeight : 0);
    buyVotes.add(dailyMacdOscillatorSlope > 0 ? dailyWeight : 0);
    buyVotes.add(dailyRsiAverage > 50 ? dailyWeight : 0);
    buyVotes.add(dailyRsiSlope > 0 ? dailyWeight : 0);
    buyVotes.add(dailyDmiPdiAverage > dailyDmiMdiAverage ? dailyWeight : 0);
    buyVotes.add(dailyDmiPdiSlope > 0 ? dailyWeight : 0);
    buyVotes.add(dailyDmiMdiSlope < 0 ? dailyWeight : 0);
    // market factor
    def marketWeight = 100;
    buyVotes.add(spxFutureEmaSlope > 0 ? marketWeight : 0);
    buyVotes.add(spxFutureMacdOscillatorAverage > 0 ? marketWeight : 0);
    buyVotes.add(spxFutureMacdOscillatorSlope > 0 ? marketWeight : 0);
    buyVotes.add(spxFutureRsiAverage > 50 ? marketWeight : 0);
    buyVotes.add(spxFutureRsiSlope > 0 ? marketWeight : 0);
    log.info("buyVotes[{}] - {}", buyVotes.average(), buyVotes);
    if(buyVotes.average() > 70) {
        hold = true;
    }
}

// 매도조건
if(priceEmaSlope < 0) {
    def sellVotes = [];
    def weight = 100;
    sellVotes.add(macdOscillatorAverage < 0 ? weight : 0);
    sellVotes.add(macdOscillatorSlope < 0 ? weight : 0);
    sellVotes.add(rsiAverage < 50 ? weight : 0);
    sellVotes.add(rsiSlope < 0 ? weight : 0);
    sellVotes.add(dmiPdiAverage < dmiMdiAverage ? weight : 0);
    sellVotes.add(dmiPdiSlope < 0 ? weight : 0);
    sellVotes.add(dmiMdiSlope > 0 ? weight : 0);
    // daily factor
    def dailyWeight = 100;
    sellVotes.add(dailyMacdOscillatorAverage < 0 ? dailyWeight : 0);
    sellVotes.add(dailyMacdOscillatorSlope < 0 ? dailyWeight : 0);
    sellVotes.add(dailyRsiAverage < 50 ? dailyWeight : 0);
    sellVotes.add(dailyRsiSlope < 0 ? dailyWeight : 0);
    sellVotes.add(dailyDmiPdiAverage < dailyDmiMdiAverage ? dailyWeight : 0);
    sellVotes.add(dailyDmiPdiSlope < 0 ? dailyWeight : 0);
    sellVotes.add(dailyDmiMdiSlope > 0 ? dailyWeight : 0);
    // market factor
    def marketWeight = 100;
    sellVotes.add(spxFutureEmaSlope < 0 ? marketWeight : 0);
    sellVotes.add(spxFutureMacdOscillatorAverage < 0 ? marketWeight : 0);
    sellVotes.add(spxFutureMacdOscillatorSlope < 0 ? marketWeight : 0);
    sellVotes.add(spxFutureRsiAverage < 50 ? marketWeight : 0);
    sellVotes.add(spxFutureRsiSlope < 0 ? marketWeight : 0);
    log.info("sellVotes[{}] - {}", sellVotes.average(), sellVotes);
    if(sellVotes.average() > 70) {
        hold = false;
    }
}

// 결과 반환
return hold;

');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','122630','KODEX 레버리지','ETF','N','50');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','229200','KODEX 코스닥150','ETF','Y','50');

-- fintics_trade: 한국투자증권 모의투자 - 지수ETF(인버스)
insert into `fintics_trade`
(`trade_id`,`name`,`enabled`,`interval`,`start_at`,`end_at`,`client_type`,`client_properties`,`alarm_id`,`hold_condition`) values
    ('7af6bc641eef4254b12dd9fa1d43384d','한국투자증권 모의투자 - 지수ETF(인버스)','N','60','09:00','15:30',
     'org.oopscraft.fintics.client.kis.KisClient','production=false
apiUrl=https://openapivts.koreainvestment.com:29443
appKey=ENC(mqcEUDQAO57SaLBCvhoz0RpFYBXtMQG2Y+NIK2jfQZ6koEUDlYx+5W8AW+eW0KVd)
appSecret=ENC(cPLNx3yraMH7FKXfEkcs0/r7ZrKDrW7nBgQ/NpKs3BeQGUkjDl+j8VG0FOhqly/DYwJdyZ5kpLMJ7GAGSv8vEZhUOlSPPP1lFNiVyWZKk+b9jhzyCQOcKs5c+Q5BxHI/A4Nhf4oVIEm8N8nQzVAypLIBQZHu3Re+aPRkWUbdArWaBI+RyLSlemy2ZsDCJh6+/kh8Bic1ooCit0Jx4y1mBZFohtf4zRGdafkkIDIL1OujMz5yRDqigYSNvcSAXRUX)
accountNo=ENC(BCcz1quNQASvnQ4/ME2CSOiufCl6GfxN)
',null,'
import java.time.LocalTime;

Boolean hold;
int period = 10;

// OHLCV
def minuteOhlcv = assetIndicator.getMinuteOhlcvs().get(0);
def dailyOhlcv = assetIndicator.getDailyOhlcvs().get(0);
log.info("minuteOhlcv:{}", minuteOhlcv);
log.info("dailyOhlcv:{}", dailyOhlcv);

// MA indicator
def priceEma = assetIndicator.getMinuteEma(60);
def priceEmaSlope = tool.slope(assetIndicator.getMinuteEmas(60), period);
def priceSma = assetIndicator.getMinuteSma(60);
def priceSmaSlope = tool.slope(assetIndicator.getMinuteSmas(60), period);

// MACD indicator
def macds = assetIndicator.getMinuteMacds(60, 120, 40);
def macdOscillatorSlope = tool.slope(macds.collect { it.oscillator }, period);
def macdOscillatorAverage = tool.average(macds.collect { it.oscillator }, period);

// RSI indicator
def rsis = assetIndicator.getMinuteRsis(60);
def rsiSlope = tool.slope(rsis, period);
def rsiAverage = tool.average(rsis, period);

// DMI indicator
def dmis = assetIndicator.getMinuteDmis(60);
def dmiPdiAverage = tool.average(dmis.collect { it.pdi }, period);
def dmiPdiSlope = tool.slope(dmis.collect { it.pdi }, period);
def dmiMdiAverage = tool.average(dmis.collect { it.mdi }, period);
def dmiMdiSlope = tool.slope(dmis.collect { it.mdi }, period);

// daily
def dailyPeriod = 3;
def dailyMacds = assetIndicator.getDailyMacds(12, 16, 9);
def dailyMacdOscillatorAverage = tool.average(dailyMacds.collect { it.oscillator }, dailyPeriod);
def dailyMacdOscillatorSlope = tool.slope(dailyMacds.collect { it.oscillator }, dailyPeriod);
def dailyRsis = assetIndicator.getDailyRsis(14);
def dailyRsiAverage = tool.average(dailyRsis, dailyPeriod);
def dailyRsiSlope = tool.slope(dailyRsis, dailyPeriod);
def dailyDmis = assetIndicator.getDailyDmis(14);
def dailyDmiPdiAverage = tool.average(dailyDmis.collect { it.pdi }, period);
def dailyDmiPdiSlope = tool.slope(dailyDmis.collect { it.pdi }, period);
def dailyDmiMdiAverage = tool.average(dailyDmis.collect { it.mdi }, period);
def dailyDmiMdiSlope = tool.slope(dailyDmis.collect { it.mdi }, period);

// market
def spxFutureIndicator = market.getSpxFutureIndicator();
def spxFutureEmaSlope = tool.slope(spxFutureIndicator.getMinuteEmas(60), period);
def spxFutureMacds = spxFutureIndicator.getMinuteMacds(60, 120, 40);
def spxFutureMacdOscillatorSlope = tool.slope(spxFutureMacds.collect { it.oscillator }, period);
def spxFutureMacdOscillatorAverage = tool.average(spxFutureMacds.collect { it.oscillator }, period);
def spxFutureRsis = spxFutureIndicator.getMinuteRsis(60);
def spxFutureRsiSlope = tool.slope(spxFutureRsis, period);
def spxFutureRsiAverage = tool.average(spxFutureRsis, period);

// 매수조건
if(priceEmaSlope > 0) {
    def buyVotes = [];
    def weight = 100;
    buyVotes.add(macdOscillatorAverage > 0 ? weight : 0);
    buyVotes.add(macdOscillatorSlope > 0 ? weight : 0);
    buyVotes.add(rsiAverage > 50 ? weight : 0);
    buyVotes.add(rsiSlope > 0 ? weight : 0);
    buyVotes.add(dmiPdiAverage > dmiMdiAverage ? weight : 0);
    buyVotes.add(dmiPdiSlope > 0 ? weight : 0);
    buyVotes.add(dmiMdiSlope < 0 ? weight : 0);
    // daily factor
    def dailyWeight = 100;
    buyVotes.add(dailyMacdOscillatorAverage > 0 ? dailyWeight : 0);
    buyVotes.add(dailyMacdOscillatorSlope > 0 ? dailyWeight : 0);
    buyVotes.add(dailyRsiAverage > 50 ? dailyWeight : 0);
    buyVotes.add(dailyRsiSlope > 0 ? dailyWeight : 0);
    buyVotes.add(dailyDmiPdiAverage > dailyDmiMdiAverage ? dailyWeight : 0);
    buyVotes.add(dailyDmiPdiSlope > 0 ? dailyWeight : 0);
    buyVotes.add(dailyDmiMdiSlope < 0 ? dailyWeight : 0);
    // market factor
    def marketWeight = 100;
    buyVotes.add(spxFutureEmaSlope > 0 ? marketWeight : 0);
    buyVotes.add(spxFutureMacdOscillatorAverage > 0 ? marketWeight : 0);
    buyVotes.add(spxFutureMacdOscillatorSlope > 0 ? marketWeight : 0);
    buyVotes.add(spxFutureRsiAverage > 50 ? marketWeight : 0);
    buyVotes.add(spxFutureRsiSlope > 0 ? marketWeight : 0);
    log.info("buyVotes[{}] - {}", buyVotes.average(), buyVotes);
    if(buyVotes.average() > 70) {
        hold = true;
    }
}

// 매도조건
if(priceEmaSlope < 0) {
    def sellVotes = [];
    def weight = 100;
    sellVotes.add(macdOscillatorAverage < 0 ? weight : 0);
    sellVotes.add(macdOscillatorSlope < 0 ? weight : 0);
    sellVotes.add(rsiAverage < 50 ? weight : 0);
    sellVotes.add(rsiSlope < 0 ? weight : 0);
    sellVotes.add(dmiPdiAverage < dmiMdiAverage ? weight : 0);
    sellVotes.add(dmiPdiSlope < 0 ? weight : 0);
    sellVotes.add(dmiMdiSlope > 0 ? weight : 0);
    // daily factor
    def dailyWeight = 100;
    sellVotes.add(dailyMacdOscillatorAverage < 0 ? dailyWeight : 0);
    sellVotes.add(dailyMacdOscillatorSlope < 0 ? dailyWeight : 0);
    sellVotes.add(dailyRsiAverage < 50 ? dailyWeight : 0);
    sellVotes.add(dailyRsiSlope < 0 ? dailyWeight : 0);
    sellVotes.add(dailyDmiPdiAverage < dailyDmiMdiAverage ? dailyWeight : 0);
    sellVotes.add(dailyDmiPdiSlope < 0 ? dailyWeight : 0);
    sellVotes.add(dailyDmiMdiSlope > 0 ? dailyWeight : 0);
    // market factor
    def marketWeight = 100;
    sellVotes.add(spxFutureEmaSlope < 0 ? marketWeight : 0);
    sellVotes.add(spxFutureMacdOscillatorAverage < 0 ? marketWeight : 0);
    sellVotes.add(spxFutureMacdOscillatorSlope < 0 ? marketWeight : 0);
    sellVotes.add(spxFutureRsiAverage < 50 ? marketWeight : 0);
    sellVotes.add(spxFutureRsiSlope < 0 ? marketWeight : 0);
    log.info("sellVotes[{}] - {}", sellVotes.average(), sellVotes);
    if(sellVotes.average() > 70) {
        hold = false;
    }
}

// 결과 반환
return hold;

');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('7af6bc641eef4254b12dd9fa1d43384d','252670','KODEX 200선물인버스2X','ETF','N','40');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('7af6bc641eef4254b12dd9fa1d43384d','251340','KODEX 코스닥150선물인버스','ETF','Y','40');
