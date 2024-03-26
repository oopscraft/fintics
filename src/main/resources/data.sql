-- delete not used data
delete from `core_menu_i18n`;
delete from `core_menu`;
delete from `core_git`;
delete from `core_alarm`;

-- core_authority
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_ASSETS','Y','Assets API Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_INDICES','Y','Indices API Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_ORDERS','Y','Orders API Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_SIMULATES','Y','Simulates API Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_TRADES','Y','Trades API Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_TRADES_EDIT','Y','Trades Edit API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_DATA','Y','Data Access API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('MONITOR','Y','Monitor Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('TRADES','Y','Trades Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('TRADE','Y','Trade Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('TRADE_EDIT','Y','Trade Edit Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('ORDERS','Y','Orders Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('SIMULATES','Y','Simulates Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('DATA','Y','Data Access Authority');

-- core_role
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('MONITOR','Y','Monitor Access Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('TRADES','Y','Trades Access Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('TRADE','Y','Trade Access Role','N','N');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('TRADE_EDIT','Y','Trade Edit Role','N','N');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('ORDERS','Y','Orders Access Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('SIMULATES','Y','Simulates Access Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('DATA','Y','Data Access Role','N','Y');

-- core_role_authority
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('MONITOR','MONITOR'),
    ('MONITOR','API_ASSETS'),
    ('MONITOR','API_TRADES');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('TRADES','TRADES'),
    ('TRADES','API_TRADES');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('TRADE','TRADE'),
    ('TRADE','API_TRADES');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('TRADE_EDIT','TRADE_EDIT'),
    ('TRADE_EDIT','API_TRADES_EDIT');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('ORDERS','ORDERS'),
    ('ORDERS','API_ORDERS');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('DATA','DATA'),
    ('DATA','API_DATA');

-- core_menu
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('monitor','Y',null,'Monitor','/monitor',1,'/static/image/icon-monitor.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('monitor','ko','모니터');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('trade','Y',null,'Trades','/trades',2,'/static/image/icon-trade.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('trade','ko','트레이드');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('order','Y',null,'Orders','/orders',3,'/static/image/icon-order.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('order','ko','거래이력');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('simulate','Y',null,'Simulates','/simulates',4,'/static/image/icon-simulate.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('simulate','ko','시뮬레이션');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('data','Y',null,'Data','/data',5,'/static/image/icon-data.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('data','ko','데이터');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`target`,`sort`,`icon`) values
    ('admin','N',null,'Admin','/admin','_blank',99,'/static/image/icon-admin.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('admin','ko','관리자');

-- core_alarm
insert into `core_alarm`
    (`alarm_id`,`alarm_name`,`alarm_client_id`,`alarm_client_config`) values
    ('fintics','Slack-Fintics','SLACK','url=https://hooks.slack.com/services/___');

-- fintics_asset
insert into `fintics_asset`
    (`asset_id`,`asset_name`,`type`) values
    ('KR.122630','KODEX 레버리지','ETF'),
    ('KR.229200','KODEX 코스닥150','ETF'),
    ('KR.252670','KODEX 200선물인버스2X','ETF'),
    ('KR.251340','KODEX 코스닥150선물인버스','ETF'),
    ('UPBIT.KRW-BTC','Bitcoin',null),
    ('UPBIT.KRW-ETH','Ethereum',null);

-- fintics_trade: 한국투자증권 모의투자 - 지수ETF
insert into `fintics_trade`
    (`trade_id`,`trade_name`,`enabled`,`interval`,`threshold`,`start_at`,`end_at`,`trade_client_id`,`trade_client_config`,`alarm_id`,`rule_script`, `order_operator_id`, `order_kind`) values
    ('06c228451ce0400fa57bb36f0568d7cb','한국투자증권 모의투자 - 지수ETF','N','60','3','09:00','15:30',
     'KIS','production=false
apiUrl=https://openapivts.koreainvestment.com:29443
appKey=[Application Key]
appSecret=[Application Secret]
accountNo=[Account Number]
',null,'
','SIMPLE','LIMIT');
insert into `fintics_trade_asset`
    (`trade_id`,`asset_id`,`enabled`, `hold_ratio`)
values
    ('06c228451ce0400fa57bb36f0568d7cb','KR.122630','N','50'),
    ('06c228451ce0400fa57bb36f0568d7cb','KR.229200','Y','50');

-- fintics_trade: 한국투자증권 모의투자 - 지수ETF(인버스)
insert into `fintics_trade`
(`trade_id`,`trade_name`,`enabled`,`interval`,`threshold`,`start_at`,`end_at`,`trade_client_id`,`trade_client_config`,`alarm_id`,`rule_script`,`order_operator_id`,`order_kind`) values
    ('7af6bc641eef4254b12dd9fa1d43384d','한국투자증권 모의투자 - 지수ETF(인버스)','N','60','3','09:00','15:30',
     'KIS','production=false
apiUrl=https://openapivts.koreainvestment.com:29443
appKey=[Application Key]
appSecret=[Application Secret]
accountNo=[Account Number]
',null,'
','SIMPLE','LIMIT');
insert into `fintics_trade_asset`
    (`trade_id`,`asset_id`,`enabled`, `hold_ratio`)
values
    ('7af6bc641eef4254b12dd9fa1d43384d','KR.252670','N','40'),
    ('7af6bc641eef4254b12dd9fa1d43384d','KR.251340','Y','40');

-- fintics_trade: 업비트 API(장시간 외 트레이드 테스트용)
insert into `fintics_trade`
(`trade_id`,`trade_name`,`enabled`,`interval`,`threshold`,`start_at`,`end_at`,`trade_client_id`,`trade_client_config`,`alarm_id`,`rule_script`,`order_operator_id`,`order_kind`) values
    ('81c6a451d6da49449faa2b5b7e66041b','코인놀이방(24시간 테스트용)','N','30','3','00:00','23:59',
     'UPBIT','accessKey=[Access Key]
secretKey=[Secret Key]',null,'

','SIMPLE','LIMIT');
insert into `fintics_trade_asset`
    (`trade_id`,`asset_id`,`enabled`, `hold_ratio`) values
    ('81c6a451d6da49449faa2b5b7e66041b','UPBIT.KRW-BTC','N','20'),
    ('81c6a451d6da49449faa2b5b7e66041b','UPBIT.KRW-ETH','N','20');

-- fintics_order
INSERT INTO fintics_order
(`order_id`, order_at, type, trade_id, asset_id, asset_name, quantity, result, error_message)
VALUES('36ac47501be24bd5b7cdb0255912e757', '2023-11-10 02:23:50.000', 'BUY', '06c228451ce0400fa57bb36f0568d7cb', 'KR.122630', 'KODEX 레버리지(테스트)', 262, 'FAILED', '모의투자 장시작전 입니다.');
INSERT INTO fintics_order
(`order_id`, order_at, type, trade_id, asset_id, asset_name, quantity, result, error_message)
VALUES('a44181a8d6424dc78682b4fa8e4b0729', '2023-11-10 09:01:12.000', 'BUY', '06c228451ce0400fa57bb36f0568d7cb', 'KR.122630', 'KODEX 레버리지(테스트)', 264, 'COMPLETED', NULL);
INSERT INTO fintics_order
(`order_id`, order_at, type, trade_id, asset_id, asset_name, quantity, result, error_message)
VALUES('62b521b88ee742239753c5b1157d7407', '2023-11-10 14:47:14.000', 'SELL', '06c228451ce0400fa57bb36f0568d7cb', 'KR.122630', 'KODEX 레버리지(테스트)', 264, 'COMPLETED', NULL);



