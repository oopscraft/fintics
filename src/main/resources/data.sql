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
    ('06c228451ce0400fa57bb36f0568d7cb','한국투자증권 모의투자 #1','N','30','09:30','15:00',
     'org.oopscraft.fintics.client.kis.KisClient','production=false
apiUrl=https://openapivts.koreainvestment.com:29443
appKey=ENC(mqcEUDQAO57SaLBCvhoz0RpFYBXtMQG2Y+NIK2jfQZ6koEUDlYx+5W8AW+eW0KVd)
appSecret=ENC(cPLNx3yraMH7FKXfEkcs0/r7ZrKDrW7nBgQ/NpKs3BeQGUkjDl+j8VG0FOhqly/DYwJdyZ5kpLMJ7GAGSv8vEZhUOlSPPP1lFNiVyWZKk+b9jhzyCQOcKs5c+Q5BxHI/A4Nhf4oVIEm8N8nQzVAypLIBQZHu3Re+aPRkWUbdArWaBI+RyLSlemy2ZsDCJh6+/kh8Bic1ooCit0Jx4y1mBZFohtf4zRGdafkkIDIL1OujMz5yRDqigYSNvcSAXRUX)
accountNo=ENC(BCcz1quNQASvnQ4/ME2CSOiufCl6GfxN)
',null,'Boolean hold;
double priceSlope = tool.slope(assetIndicator.getMinutePrices(), 10);
double emaSlope = tool.slope(assetIndicator.getMinuteEmas(10), 10);
double smaSlope = tool.slope(assetIndicator.getMinuteSmas(10), 10);
double macdOscillator = assetIndicator.getMinuteMacd(12, 26, 9).getOscillator();
double macdOscillatorSlope = tool.slope(assetIndicator.getMinuteMacds(12, 26, 9).collect { it.oscillator }, 10);
double macdOscillatorAverage = tool.average(assetIndicator.getMinuteMacds(12, 26, 9).collect { it.oscillator }, 10);
double rsi = assetIndicator.getMinuteRsi(14);
double rsiSlope = tool.slope(assetIndicator.getMinuteRsis(14), 10);
double rsiAverage = tool.average(assetIndicator.getMinuteRsis(14), 10);

log.info("priceSlope:{}, emaSlope:{}, smaSlope:{} , macdOscillator:{}, macdOscillatorSlope:{}, macdOscillatorAverage:{}, rsi:{}, rsiSlope:{}",
priceSlope, emaSlope, smaSlope, macdOscillator, macdOscillatorSlope, macdOscillatorAverage, rsi, rsiSlope);

// 보유 조건 - 가격,SMA,EMA,MACD 모두 상승중인 경우
if(priceSlope > 0
&& emaSlope > 0
&& smaSlope > 0
&& macdOscillatorAverage > 0
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

');

-- fintics_trade_asset
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','122630','KODEX 레버리지','ETF','Y','25');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','252670','KODEX 200선물인버스2X','ETF','Y','25');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','229200','KODEX 코스닥150','ETF','Y','25');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','251340','KODEX 코스닥150선물인버스','ETF','Y','25');
