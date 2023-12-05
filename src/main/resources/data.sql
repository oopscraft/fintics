-- delete not used data
delete from `core_menu_i18n`;
delete from `core_menu`;
delete from `core_git`;
delete from `core_alarm`;

-- core_authority
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('MONITOR','Y','Monitor Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('TRADE','Y','Trade Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('TRADE_EDIT','Y','Trade Edit Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('ORDER','Y','Order Access Authority');

-- core_role_authority
insert into `core_role_authority` (`role_id`,`authority_id`) values ('USER', 'MONITOR');
insert into `core_role_authority` (`role_id`,`authority_id`) values ('USER', 'TRADE');
insert into `core_role_authority` (`role_id`,`authority_id`) values ('USER', 'ORDER');

-- core_menu
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('monitor','Y',null,'Monitor','/monitor',1,'/static/image/icon-monitor.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('monitor','ko','모니터');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('trade','Y',null,'Trade','/trade',2,'/static/image/icon-trade.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('trade','ko','트레이드');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('order','Y',null,'Order','/order',3,'/static/image/icon-order.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('order','ko','거래이력');
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

');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','122630','KODEX 레버리지','N','50');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','229200','KODEX 코스닥150','Y','50');

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

');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`, `enabled`, `hold_ratio`) values
    ('7af6bc641eef4254b12dd9fa1d43384d','252670','KODEX 200선물인버스2X','N','40');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`, `enabled`, `hold_ratio`) values
    ('7af6bc641eef4254b12dd9fa1d43384d','251340','KODEX 코스닥150선물인버스','Y','40');

-- fintics_trade: 업비트 API(장시간 외 트레이드 테스트용)
insert into `fintics_trade`
(`trade_id`,`name`,`enabled`,`interval`,`start_at`,`end_at`,`client_type`,`client_properties`,`alarm_id`,`hold_condition`) values
    ('81c6a451d6da49449faa2b5b7e66041b','코인놀이방(24시간 테스트용)','N','30','00:00','23:59',
     'org.oopscraft.fintics.client.trade.upbit.UpbitTradeClient','accessKey=[발급 accessKey]
secretKey=[발급 secretKey]',null,'

');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`enabled`, `hold_ratio`) values
    ('81c6a451d6da49449faa2b5b7e66041b','KRW-BTC','Bitcoin','N','20');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`enabled`, `hold_ratio`) values
    ('81c6a451d6da49449faa2b5b7e66041b','KRW-ETH','Ethereum','N','20');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`enabled`, `hold_ratio`) values
    ('81c6a451d6da49449faa2b5b7e66041b','KRW-AVAX','Avalanche','N','20');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`enabled`, `hold_ratio`) values
    ('81c6a451d6da49449faa2b5b7e66041b','KRW-SOL','Solana','N','20');

-- fintics_order
INSERT INTO fintics_order
(order_id, order_at, order_kind, trade_id, symbol, asset_name, quantity, order_result, error_message)
VALUES('36ac47501be24bd5b7cdb0255912e757', '2023-11-10 02:23:50.000', 'BUY', '06c228451ce0400fa57bb36f0568d7cb', '122630', 'KODEX 레버리지(테스트)', 262, 'FAILED', '모의투자 장시작전 입니다.');
INSERT INTO fintics_order
(order_id, order_at, order_kind, trade_id, symbol, asset_name, quantity, order_result, error_message)
VALUES('a44181a8d6424dc78682b4fa8e4b0729', '2023-11-10 09:01:12.000', 'BUY', '06c228451ce0400fa57bb36f0568d7cb', '122630', 'KODEX 레버리지(테스트)', 264, 'COMPLETED', NULL);
INSERT INTO fintics_order
(order_id, order_at, order_kind, trade_id, symbol, asset_name, quantity, order_result, error_message)
VALUES('62b521b88ee742239753c5b1157d7407', '2023-11-10 14:47:14.000', 'SELL', '06c228451ce0400fa57bb36f0568d7cb', '122630', 'KODEX 레버리지(테스트)', 264, 'COMPLETED', NULL);



