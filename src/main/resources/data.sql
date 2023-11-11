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
     'org.oopscraft.fintics.client.trade.kis.KisTradeClient','production=false
apiUrl=https://openapivts.koreainvestment.com:29443
appKey=ENC(mqcEUDQAO57SaLBCvhoz0RpFYBXtMQG2Y+NIK2jfQZ6koEUDlYx+5W8AW+eW0KVd)
appSecret=ENC(cPLNx3yraMH7FKXfEkcs0/r7ZrKDrW7nBgQ/NpKs3BeQGUkjDl+j8VG0FOhqly/DYwJdyZ5kpLMJ7GAGSv8vEZhUOlSPPP1lFNiVyWZKk+b9jhzyCQOcKs5c+Q5BxHI/A4Nhf4oVIEm8N8nQzVAypLIBQZHu3Re+aPRkWUbdArWaBI+RyLSlemy2ZsDCJh6+/kh8Bic1ooCit0Jx4y1mBZFohtf4zRGdafkkIDIL1OujMz5yRDqigYSNvcSAXRUX)
accountNo=ENC(BCcz1quNQASvnQ4/ME2CSOiufCl6GfxN)
',null,'
Boolean hold;
int period = 10;

// EMA
def assetEmas = assetIndicator.getMinuteEmas(60);
def assetEmaSlope = tool.slope(assetEmas, period);
log.info("assetEmaSlope:{}", assetEmaSlope);

// MACD
def assetMacds = assetIndicator.getMinuteMacds(60, 120, 40);
def assetMacdOscillatorAverage = tool.average(assetMacds.collect{it.oscillator}, period);
log.info("assetMacdOscillatorAverage:{}", assetMacdOscillatorAverage);

// RSI
def assetRsis = assetIndicator.getMinuteRsis(60);
def assetRsiAverage = tool.average(assetRsis, period);
log.info("assetRsiAverage:{}", assetRsiAverage);

// DMI
def assetDmis = assetIndicator.getMinuteDmis(60);
def assetDmiPdiAverage = tool.average(assetDmis.collect{it.pdi}, period);
def assetDmiMdiAverage = tool.average(assetDmis.collect{it.mdi}, period);
log.info("assetDmiPdiAverage:{}", assetDmiPdiAverage);
log.info("assetDmiMdiAverage:{}", assetDmiMdiAverage);

// Kospi
def kospiIndicator = indiceIndicators[''KOSPI''];
def kospiEmas = kospiIndicator.getMinuteEmas(60);
def kospiEmaSlope = tool.slope(kospiEmas, period);
log.info("kospiEmaSlope:{}", kospiEmaSlope);

// USD/KRW
def usdKrwIndicator = indiceIndicators[''USD_KRW''];
def usdKrwEmas = usdKrwIndicator.getMinuteEmas(60);
def usdKrwEmaSlope = tool.slope(usdKrwEmas, period);
log.info("usdKrwEmaSlope:{}", usdKrwEmaSlope);

// Nasdaq Future
def ndxFutureIndicator = indiceIndicators[''NDX_FUTURE''];
def ndxFutureEmas = ndxFutureIndicator.getMinuteEmas(60);
def ndxFutureEmaSlope = tool.slope(ndxFutureEmas, period);
log.info("ndxFutureEmaSlope:{}", ndxFutureEmaSlope);

// 매수조건
if(assetEmaSlope > 0) {
    def buyVotes = [];

    // 대상종목 상승시 매수
    buyVotes.add(assetEmaSlope > 0 ? 100 : 0);
    buyVotes.add(assetMacdOscillatorAverage > 0 ? 100 : 0);
    buyVotes.add(assetRsiAverage > 50 ? 100 : 0);
    buyVotes.add(assetDmiPdiAverage > assetDmiMdiAverage ? 100 : 0);

    // 코스피 상승시 매수
    buyVotes.add(kospiEmaSlope > 0 ? 100 : 0);

    // 달러환율 하락시 매수
    buyVotes.add(usdKrwEmaSlope < 0 ? 100 : 0);

    // 나스닥선물 상승시 매수
    buyVotes.add(ndxFutureEmaSlope > 0 ? 100 : 0);

    // 매수여부 결과
    log.info("buyVotes[{}] - {}", buyVotes.average(), buyVotes);
    if(buyVotes.average() > 70) {
        hold = true;
    }
}

// 매도조건
if(assetEmaSlope < 0) {
    def sellVotes = [];

    // 대상종목 하락시 매도
    sellVotes.add(assetEmaSlope < 0 ? 100 : 0);
    sellVotes.add(assetMacdOscillatorAverage < 0 ? 100 : 0);
    sellVotes.add(assetRsiAverage < 50 ? 100 : 0);
    sellVotes.add(assetDmiPdiAverage < assetDmiMdiAverage ? 100 : 0);

    // 코스피 하락시 매도
    sellVotes.add(kospiEmaSlope < 0 ? 100 : 0);

    // 달러환율 상승시 매도
    sellVotes.add(usdKrwEmaSlope > 0 ? 100 : 0);

    // 나스닥선물 하락시 매도
    sellVotes.add(ndxFutureEmaSlope < 0 ? 100 : 0);

    // 매도여부 결과
    log.info("sellVotes[{}] - {}", sellVotes.average(), sellVotes);
    if(sellVotes.average() > 30) {
        hold = false;
    }
}

// 결과반환
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
     'org.oopscraft.fintics.client.trade.kis.KisTradeClient','production=false
apiUrl=https://openapivts.koreainvestment.com:29443
appKey=ENC(mqcEUDQAO57SaLBCvhoz0RpFYBXtMQG2Y+NIK2jfQZ6koEUDlYx+5W8AW+eW0KVd)
appSecret=ENC(cPLNx3yraMH7FKXfEkcs0/r7ZrKDrW7nBgQ/NpKs3BeQGUkjDl+j8VG0FOhqly/DYwJdyZ5kpLMJ7GAGSv8vEZhUOlSPPP1lFNiVyWZKk+b9jhzyCQOcKs5c+Q5BxHI/A4Nhf4oVIEm8N8nQzVAypLIBQZHu3Re+aPRkWUbdArWaBI+RyLSlemy2ZsDCJh6+/kh8Bic1ooCit0Jx4y1mBZFohtf4zRGdafkkIDIL1OujMz5yRDqigYSNvcSAXRUX)
accountNo=ENC(BCcz1quNQASvnQ4/ME2CSOiufCl6GfxN)
',null,'
Boolean hold;
int period = 10;

// EMA
def assetEmas = assetIndicator.getMinuteEmas(60);
def assetEmaSlope = tool.slope(assetEmas, period);
log.info("assetEmaSlope:{}", assetEmaSlope);

// MACD
def assetMacds = assetIndicator.getMinuteMacds(60, 120, 40);
def assetMacdOscillatorAverage = tool.average(assetMacds.collect{it.oscillator}, period);
log.info("assetMacdOscillatorAverage:{}", assetMacdOscillatorAverage);

// RSI
def assetRsis = assetIndicator.getMinuteRsis(60);
def assetRsiAverage = tool.average(assetRsis, period);
log.info("assetRsiAverage:{}", assetRsiAverage);

// DMI
def assetDmis = assetIndicator.getMinuteDmis(60);
def assetDmiPdiAverage = tool.average(assetDmis.collect{it.pdi}, period);
def assetDmiMdiAverage = tool.average(assetDmis.collect{it.mdi}, period);
log.info("assetDmiPdiAverage:{}", assetDmiPdiAverage);
log.info("assetDmiMdiAverage:{}", assetDmiMdiAverage);

// Kospi
def kospiIndicator = indiceIndicators[''KOSPI''];
def kospiEmas = kospiIndicator.getMinuteEmas(60);
def kospiEmaSlope = tool.slope(kospiEmas, period);
log.info("kospiEmaSlope:{}", kospiEmaSlope);

// USD/KRW
def usdKrwIndicator = indiceIndicators[''USD_KRW''];
def usdKrwEmas = usdKrwIndicator.getMinuteEmas(60);
def usdKrwEmaSlope = tool.slope(usdKrwEmas, period);
log.info("usdKrwEmaSlope:{}", usdKrwEmaSlope);

// Nasdaq Future
def ndxFutureIndicator = indiceIndicators[''NDX_FUTURE''];
def ndxFutureEmas = ndxFutureIndicator.getMinuteEmas(60);
def ndxFutureEmaSlope = tool.slope(ndxFutureEmas, period);
log.info("ndxFutureEmaSlope:{}", ndxFutureEmaSlope);

// 매수조건(인버스)
if(assetEmaSlope > 0) {
    def buyVotes = [];

    // 대상종목 상승시 매수
    buyVotes.add(assetEmaSlope > 0 ? 100 : 0);
    buyVotes.add(assetMacdOscillatorAverage > 0 ? 100 : 0);
    buyVotes.add(assetRsiAverage > 50 ? 100 : 0);
    buyVotes.add(assetDmiPdiAverage > assetDmiMdiAverage ? 100 : 0);

    // 코스피 하락시 매수(인버스)
    buyVotes.add(kospiEmaSlope < 0 ? 100 : 0);

    // 달러환율 상승시 매수(인버스)
    buyVotes.add(usdKrwEmaSlope > 0 ? 100 : 0);

    // 나스닥선물 하락시 매수(인버스)
    buyVotes.add(ndxFutureEmaSlope < 0 ? 100 : 0);

    // 매수여부 결과
    log.info("buyVotes[{}] - {}", buyVotes.average(), buyVotes);
    if(buyVotes.average() > 70) {
        hold = true;
    }
}

// 매도조건(인버스)
if(assetEmaSlope < 0) {
    def sellVotes = [];

    // 대상종목 하락시 매도
    sellVotes.add(assetEmaSlope < 0 ? 100 : 0);
    sellVotes.add(assetMacdOscillatorAverage < 0 ? 100 : 0);
    sellVotes.add(assetRsiAverage < 50 ? 100 : 0);
    sellVotes.add(assetDmiPdiAverage < assetDmiMdiAverage ? 100 : 0);

    // 코스피 상승시 매도(인버스)
    sellVotes.add(kospiEmaSlope > 0 ? 100 : 0);

    // 달러환율 하락시 매도(인버스)
    sellVotes.add(usdKrwEmaSlope < 0 ? 100 : 0);

    // 나스닥선물 상승시 매도(인버스)
    sellVotes.add(ndxFutureEmaSlope > 0 ? 100 : 0);

    // 매도여부 결과
    log.info("sellVotes[{}] - {}", sellVotes.average(), sellVotes);
    if(sellVotes.average() > 30) {
        hold = false;
    }
}

// 결과반환
return hold;

');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('7af6bc641eef4254b12dd9fa1d43384d','252670','KODEX 200선물인버스2X','ETF','N','40');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('7af6bc641eef4254b12dd9fa1d43384d','251340','KODEX 코스닥150선물인버스','ETF','Y','40');
