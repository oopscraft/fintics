-- delete not used data
delete from `core_menu_i18n`;
delete from `core_menu`;
delete from `core_git`;
delete from `core_alarm`;

-- core_authority
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('TRADE','Y','Trade Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('TRADE_EDIT','Y','Trade Edit Authority');

-- core_role_authority
insert into `core_role_authority` (`role_id`,`authority_id`) values ('ANONYMOUS', 'TRADE');

-- core_alarm
insert into `core_alarm`
    (`alarm_id`,`alarm_name`,`client_type`,`client_config`) values
    ('fintics','Fintics','org.oopscraft.arch4j.core.alarm.client.slack.SlackAlarmClient','url=https://hooks.slack.com/services/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');

-- fintics_trade
insert into `fintics_trade`
    (`trade_id`,`name`,`enabled`,`interval`,`client_type`,`client_properties`) values
    ('06c228451ce0400fa57bb36f0568d7cb','한국투자증권 모의투자 #1','Y','30',
     'org.oopscraft.fintics.client.kis.KisClient','production=false
apiUrl=https://openapivts.koreainvestment.com:29443
appKey=PSEoPN1uMGYe67XzUuaEDhqpMYOJNumYb2yl
appSecret=kLdK3wGPQM3aS402GWetUxccOx4xMZbQZOYpxo3JJNearF64mjBjqG/msDdH9yCLWCGnSySvRw7HxClEMNtZ3ChF0TiSQxBdJfPerfQRyqLBRNmsdSaUOvHaiulNtTPvlOheqk16+0Ce4X7yyM0/DfTp5sZIP5mh2WCXkhF5wCwhUcOKLZk=
accountNo=50096055-01
');

-- fintics_trade_asset
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','229200','KODEX 코스닥150','ETF','Y','10');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','251340','KODEX 코스닥150선물인버스','ETF','Y','10');
