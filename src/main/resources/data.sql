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
appKey=PSTxd5BurtHzO3Viit4rL4Syhuz8RyzxuyJL
appSecret=dMrEmCtbBQS2+oMUDUBgnKDVKrns3RqVYOGkBG8/Zxpm5M1M92yEoelrWenmn5CLCCgncwgneBqrV/GOiNpTOxrYVrYWLJVTbBRTSeqsH60nJXZ2lrjhfAxWNKzcXeyR8EbWcid4bQDTL3vtrQmCz+Jazky5cm5fx0lBs7ciL33x7EIp+5w=
accountNo=50096055-01
','fintics','double priceSlope = tool.slope(assetIndicator.getMinutePrices(), 10);
double smaSlope = tool.slope(assetIndicator.getMinuteSmas(10), 10);
double smaLongSlope = tool.slope(assetIndicator.getMinuteSmas(20), 20);
double macdOscillator = assetIndicator.getMinuteMacd(12, 26, 9).getOscillator();
double macdOscillatorSlope = tool.slope(assetIndicator.getMinuteMacds(12, 26, 9).collect { it.oscillator }, 10);
double rsi = assetIndicator.getMinuteRsi(14);
double rsiSlope = tool.slope(assetIndicator.getMinuteRsis(14), 10);

log.debug("{}", assetIndicator.getMinutePrices());
log.info("priceSlope:{}, smaSlope:{}, smaLongSlope:{} , macdOscillator:{}, macdOscillatorSlope:{}, rsi:{}, rsiSlope:{}",
priceSlope, smaSlope, smaLongSlope, macdOscillator, macdOscillatorSlope, rsi, rsiSlope);

// 매수 조건 - 가격,MACD,RSI 모두 상승중인 경우
if(priceSlope > 0
&& smaSlope > 0
&& macdOscillatorSlope > 0
&& rsiSlope > 0
) {
    // 상승추세일 경우 즉각 매수
    if(macdOscillator > 0) {
        return true;
    }
    // 하락추세일 경우 일시적인 상승인지 이동평균선 체크 후 매수
    if(macdOscillator < 0) {
        if(smaLongSlope > 0) {
            return true;
        }
    }
}

// 매도조건 - 가격,MACD,RSI 모두 하락중인 경우
if(priceSlope < 0
&& smaSlope < 0
&& macdOscillatorSlope < 0
&& rsiSlope < 0
) {
    // 하락추세시에는 즉각 매도
    if(macdOscillator < 0) {
        return false;
    }
    // 상승추세시에는 일시적인 하락인지 이동평균선 체크 후 매도
    if(macdOscillator > 0) {
        if(smaLongSlope < 0) {
            return false
        }
    }
}
');

-- fintics_trade_asset
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','122630','KODEX 레버리지','ETF','Y','20');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','252670','KODEX 200선물인버스2X','ETF','Y','20');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','229200','KODEX 코스닥150','ETF','Y','20');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','251340','KODEX 코스닥150선물인버스','ETF','Y','20');
