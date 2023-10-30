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

-- fintics_trade
insert into `fintics_trade`
    (`trade_id`,`name`,`enabled`,`interval`,`start_at`,`end_at`,`client_type`,`client_properties`,`alarm_id`,`hold_condition`) values
    ('06c228451ce0400fa57bb36f0568d7cb','한국투자증권 모의투자 #1','N','30','09:00','15:30',
     'org.oopscraft.fintics.client.kis.KisClient','production=false
apiUrl=https://openapivts.koreainvestment.com:29443
appKey=ENC(mqcEUDQAO57SaLBCvhoz0RpFYBXtMQG2Y+NIK2jfQZ6koEUDlYx+5W8AW+eW0KVd)
appSecret=ENC(cPLNx3yraMH7FKXfEkcs0/r7ZrKDrW7nBgQ/NpKs3BeQGUkjDl+j8VG0FOhqly/DYwJdyZ5kpLMJ7GAGSv8vEZhUOlSPPP1lFNiVyWZKk+b9jhzyCQOcKs5c+Q5BxHI/A4Nhf4oVIEm8N8nQzVAypLIBQZHu3Re+aPRkWUbdArWaBI+RyLSlemy2ZsDCJh6+/kh8Bic1ooCit0Jx4y1mBZFohtf4zRGdafkkIDIL1OujMz5yRDqigYSNvcSAXRUX)
accountNo=ENC(BCcz1quNQASvnQ4/ME2CSOiufCl6GfxN)
',null,'import java.time.LocalTime;

int period = 10;
Boolean hold;
def time = assetIndicator.getMinuteDateTimes().get(0).toLocalTime();
def price = assetIndicator.getMinutePrices().get(0);

// MA indicator
def priceEma = assetIndicator.getMinuteEma(60);
def priceEmaSlope = tool.slope(assetIndicator.getMinuteEmas(60), period);
def priceSma = assetIndicator.getMinuteSma(60);
def priceSmaSlope = tool.slope(assetIndicator.getMinuteSmas(60), period);

// MACD indicator
def macds = assetIndicator.getMinuteMacds(60, 120, 30);
def macdOscillatorSlope = tool.slope(macds.collect { it.oscillator }, period);
def macdOscillatorAverage = tool.average(macds.collect { it.oscillator }, period);

// RSI indicator
def rsis = assetIndicator.getMinuteRsis(60);
def rsiSlope = tool.slope(rsis, period);
def rsiAverage = tool.average(rsis, period);

// DMI indicator
def dmis = assetIndicator.getMinuteDmis(60);
def dmiPdiSlope = tool.slope(dmis.collect { it.pdi }, period);
def dmiPdiAverage = tool.average(dmis.collect { it.pdi }, period);
def dmiMdiSlope = tool.slope(dmis.collect { it.mdi }, period);
def dmiMdiAverage = tool.average(dmis.collect { it.mdi }, period);
def dmiAdxSlope = tool.slope(dmis.collect { it.adx }, period);
def dmiAdxAverage = tool.average(dmis.collect { it.adx }, period);

log.info(
        "dateTime:{}, price:{}, priceEma:{}, priceEmaSlope:{}, priceSma:{}, priceSmaSlope:{} " +
        "macdOscillatorSlope:{}, macdOscillatorAverage:{}, " +
        "rsiSlope:{}, rsiAverage:{}, " +
        "dmiPdiSlope:{}, dmiPdiAverage:{}, dmiMdiSlope:{}, dmiMdiAverage:{}, dmiAdxSlope:{}, dmiAdxAverage:{}",
        dateTime, price, priceEma, priceEmaSlope, priceSma, priceSmaSlope,
        macdOscillatorSlope, macdOscillatorAverage,
        rsiSlope, rsiAverage,
        dmiPdiSlope, dmiPdiAverage, dmiMdiSlope, dmiMdiAverage, dmiAdxSlope, dmiAdxAverage
);

// 매수조건
if(priceEmaSlope > 0) {
    if(1 == 1
            && macdOscillatorAverage > 0
            && rsiAverage > 50
            && dmiPdiAverage > dmiMdiAverage
    ){
        hold = true;
}
}

// 매도조건
if(priceEmaSlope < 0) {
    if(1 == 1
            && macdOscillatorAverage < 0
            && rsiAverage < 50
            && dmiPdiAverage < dmiMdiAverage
    ) {
        hold = false;
}
}

// 장종료전 모두 청산(보유하지 않음)
if(time.isAfter(LocalTime.of(15,15))) {
    hold = false;
}

// 결과 반환
return hold;

');

-- fintics_trade_asset
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','229200','KODEX 코스닥150','ETF','Y','50');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','251340','KODEX 코스닥150선물인버스','ETF','Y','50');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','122630','KODEX 레버리지','ETF','N','50');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','252670','KODEX 200선물인버스2X','ETF','N','50');
