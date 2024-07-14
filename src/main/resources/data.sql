-- delete not used data
delete from `core_menu_i18n`;
delete from `core_menu`;
delete from `core_git`;
delete from `core_alarm`;

-- core_authority
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_ASSETS','Y','Assets Access API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_ASSETS_EDIT','Y','Assets Edit API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_OHLCVS','Y','Ohlcvs API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_NEWSES','Y','News API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_ORDERS','Y','Orders API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_SIMULATES','Y','Simulates API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_SIMULATES_EDIT','Y','Simulates Edit API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_TRADES','Y','Trades API Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_TRADES_EDIT','Y','Trades Edit API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_BASKETS','Y','Baskets API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_BASKETS_EDIT','Y','Baskets Edit API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_STRATEGIES','Y','Strategies API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_STRATEGIES_EDIT','Y','Strategies Edit API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_BROKERS','Y','Brokers API Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_BROKERS_EDIT','Y','Brokers Edit API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_DATA','Y','Data Access API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('API_DATA_EDIT','Y','Data Edit API Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('MONITORS','Y','Monitor Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('TRADES','Y','Trades Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('TRADES_EDIT','Y','Trade Edit Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('ASSETS','Y','Assets Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('ASSETS_EDIT','Y','Assets Edit Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('BASKETS','Y','Baskets Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('BASKETS_EDIT','Y','Baskets Edit Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('STRATEGIES','Y','Strategies Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('STRATEGIES_EDIT','Y','Strategies Edit Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('BROKERS','Y','Brokers Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('BROKERS_EDIT','Y','Brokers Edit Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('ORDERS','Y','Orders Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('SIMULATES','Y','Simulates Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('SIMULATES_EDIT','Y','Simulates Edit Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('DATA','Y','Data Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('DATA_EDIT','Y','Data Edit Authority');

-- core_role
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('MONITORS','Y','Monitors Access Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('OHLCVS','Y','Ohlcvs Access Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('TRADES','Y','Trades Access Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('TRADES_EDIT','Y','Trades Edit Role','N','N');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('ASSETS','Y','Assets Access Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('ASSETS_EDIT','Y','Assets Edit Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('BASKETS','Y','Baskets Access Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('BASKETS_EDIT','Y','Baskets Edit Role','N','N');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('STRATEGIES','Y','Strategies Access Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('STRATEGIES_EDIT','Y','Strategies Edit Role','N','N');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('BROKERS','Y','Brokers Access Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('BROKERS_EDIT','Y','Brokers Edit Role','N','N');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('ORDERS','Y','Orders Access Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('SIMULATES','Y','Simulates Access Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('SIMULATES_EDIT','Y','Simulates Edit Role','N','N');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('DATA','Y','Data Access Role','N','Y');
insert into `core_role` (`role_id`,`system_required`,`role_name`,`anonymous`,`authenticated`) values
    ('DATA_EDIT','Y','Data Edit Role','N','N');

-- core_role_authority
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('MONITORS','MONITORS'),
    ('MONITORS','API_ASSETS'),
    ('MONITORS','API_OHLCVS'),
    ('MONITORS','API_NEWSES'),
    ('MONITORS','API_TRADES');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('TRADES','TRADES'),
    ('TRADES','API_TRADES');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('TRADES_EDIT','TRADES_EDIT'),
    ('TRADES_EDIT','API_TRADES_EDIT');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('ASSETS','ASSETS'),
    ('ASSETS','API_ASSETS');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('ASSETS_EDIT','ASSETS_EDIT'),
    ('ASSETS_EDIT','API_ASSETS_EDIT');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('BASKETS','BASKETS'),
    ('BASKETS','API_BASKETS');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('BASKETS_EDIT','BASKETS_EDIT'),
    ('BASKETS_EDIT','API_BASKETS_EDIT');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('STRATEGIES','STRATEGIES'),
    ('STRATEGIES','API_STRATEGIES');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('STRATEGIES_EDIT','STRATEGIES_EDIT'),
    ('STRATEGIES_EDIT','API_STRATEGIES_EDIT');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('BROKERS','BROKERS'),
    ('BROKERS','API_BROKERS');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('BROKERS_EDIT','BROKERS_EDIT'),
    ('BROKERS_EDIT','API_BROKERS_EDIT');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('SIMULATES','SIMULATES'),
    ('SIMULATES','API_SIMULATES');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('SIMULATES_EDIT','SIMULATES_EDIT'),
    ('SIMULATES_EDIT','API_SIMULATES_EDIT');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('ORDERS','ORDERS'),
    ('ORDERS','API_ORDERS');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('DATA','DATA'),
    ('DATA','API_DATA'),
    ('DATA','API_ASSETS'),
    ('DATA','API_OHLCVS'),
    ('DATA','API_NEWSES');
insert into `core_role_authority` (`role_id`,`authority_id`) values
    ('DATA_EDIT','DATA_EDIT'),
    ('DATA_EDIT','API_DATA_EDIT');

-- core_menu
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('monitors','Y',null,'Monitors','/monitors',1,'/static/image/icon-monitor.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('monitors','ko','모니터');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('trades','Y',null,'Trades','/trades',2,'/static/image/icon-trade.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('trades','ko','트레이드');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('basket','Y',null,'Basket','/baskets',3,'/static/image/icon-basket.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('basket','ko','바스켓');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('strategies','Y',null,'Strategies','/strategies',4,'/static/image/icon-strategy.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('strategies','ko','매매전략');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('brokers','Y',null,'Brokers','/brokers',5,'/static/image/icon-broker.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('brokers','ko','브로커');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('assets','Y',null,'Assets','/assets',6,'/static/image/icon-asset.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('assets','ko','종목');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('orders','Y',null,'Orders','/orders',7,'/static/image/icon-order.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('orders','ko','거래이력');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('simulates','Y',null,'Simulates','/simulates',8,'/static/image/icon-simulate.svg');
insert into `core_menu_i18n` (`menu_id`,`language`,`menu_name`) values
    ('simulates','ko','시뮬레이션');
insert into `core_menu` (`menu_id`,`system_required`,`parent_menu_id`,`menu_name`,`link`,`sort`,`icon`) values
    ('data','Y',null,'Data','/data',9,'/static/image/icon-data.svg');
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
    (`asset_id`,`asset_name`,`market`,`exchange`,`type`) values
    ('KR.122630','KODEX 레버리지','KR','XKRX','ETF'),
    ('KR.229200','KODEX 코스닥150','KR','XKRX','ETF'),
    ('KR.252670','KODEX 200선물인버스2X','KR','XKRX','ETF'),
    ('KR.251340','KODEX 코스닥150선물인버스','KR','XKRX','ETF'),
    ('KR.005930','삼성전자','KR','XKRX','STOCK'),
    ('KR.000660','에스케이하이닉스','KR','XKRX','STOCK'),
    ('US.SPY','SPDR S&P 500','US','XASE','ETF'),
    ('US.QQQ','Invesco QQQ Trust Series 1','US','XNAS','ETF'),
    ('US.AAPL','Apple Inc. Common Stock','US','XNAS','STOCK'),
    ('US.MSFT','Microsoft Corporation Common Stock','US','XNAS','STOCK'),
    ('UPBIT.KRW-BTC','Bitcoin','UPBIT','UPBIT',null),
    ('UPBIT.KRW-ETH','Ethereum','UPBIT','UPBIT',null);

-- fintics_broker
insert into `fintics_broker`
    (`broker_id`,`broker_name`,`broker_client_id`,`broker_client_properties`) values
    ('ca5f55cd88694715bcb4c478710d9a68','Korea Investment Test','KIS',null),
    ('961eb9c68c9547ce9ae61bbe3be7f037','Korea Investment US Test','KIS_US',null),
    ('a135ee9a276f4edf81d6e1b6b9d31e39','Upbit Test','UPBIT',null);

-- fintics_basket - 국내대형주
insert into `fintics_basket`
    (`basket_id`, `basket_name`, `market`) values
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','국내대형주', 'KR');
insert into `fintics_basket_asset`
    (`basket_id`,`asset_id`,`enabled`, `holding_weight`)
values
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','KR.122630','Y','20'),
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','KR.229200','Y','20'),
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','KR.005930','Y','20'),
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','KR.000660','Y','20');

-- fintics_basket - 미국대형주
insert into `fintics_basket`
    (`basket_id`, `basket_name`,`market`) values
    ('a920f8813c6f46fda2947cee1c8cfb1d','미국대형주', 'US');
insert into `fintics_basket_asset`
    (`basket_id`,`asset_id`,`enabled`, `holding_weight`)
values
    ('a920f8813c6f46fda2947cee1c8cfb1d','US.SPY','Y','20'),
    ('a920f8813c6f46fda2947cee1c8cfb1d','US.QQQ','Y','20'),
    ('a920f8813c6f46fda2947cee1c8cfb1d','US.AAPL','Y','20'),
    ('a920f8813c6f46fda2947cee1c8cfb1d','US.MSFT','Y','20');

-- fintics_basket - Upbit (24시간 테스트용)
insert into `fintics_basket`
(`basket_id`, `basket_name`, `market`) values
    ('7818b580e3f340498b97f50e0e801ff8','Upbit (24시간 테스트용)', 'UPBIT');
insert into `fintics_basket_asset`
(`basket_id`,`asset_id`,`enabled`, `holding_weight`)
values
    ('7818b580e3f340498b97f50e0e801ff8','UPBIT.KRW-BTC','Y','40'),
    ('7818b580e3f340498b97f50e0e801ff8','UPBIT.KRW-ETH','Y','40');

-- fintics_strategy
insert into `fintics_strategy`
    (`strategy_id`,`strategy_name`,`language`,`script`) values
    ('7c94187b346f4727a0f2478fdc53064f','Test Rule','GROOVY','return null');

-- fintics_trade: 한국투자증권 모의투자 - 국내
insert into `fintics_trade`
    (`trade_id`,`trade_name`,`enabled`,`interval`,`threshold`,`start_at`,`end_at`,`invest_amount`,`broker_id`,`basket_id`,`strategy_id`,`strategy_variables`,`alarm_id`,`order_kind`) values
    ('06c228451ce0400fa57bb36f0568d7cb','한국투자증권 모의투자 - 국내','Y','60','2','09:00','15:30','1000000',
     'ca5f55cd88694715bcb4c478710d9a68','e5b2dda4ede54176b5e01eed7c4b9ed8','7c94187b346f4727a0f2478fdc53064f', null, null, 'LIMIT');

-- fintics_trade: 한국투자증권 모의투자 - 미국
insert into `fintics_trade`
    (`trade_id`,`trade_name`,`enabled`,`interval`,`threshold`,`start_at`,`end_at`,`invest_amount`,`broker_id`,`basket_id`,`strategy_id`,`strategy_variables`,`alarm_id`,`order_kind`) values
    ('7af6bc641eef4254b12dd9fa1d43384d','한국투자증권 모의투자 - 미국','Y','60','2','09:30','16:00','1000',
     '961eb9c68c9547ce9ae61bbe3be7f037','a920f8813c6f46fda2947cee1c8cfb1d','7c94187b346f4727a0f2478fdc53064f', null, null, 'LIMIT');

-- fintics_trade: 업비트 API(장시간 외 트레이드 테스트용)
insert into `fintics_trade`
    (`trade_id`,`trade_name`,`enabled`,`interval`,`threshold`,`start_at`,`end_at`,`invest_amount`,`broker_id`,`basket_id`,`strategy_id`,`strategy_variables`,`alarm_id`,`order_kind`) values
    ('81c6a451d6da49449faa2b5b7e66041b','코인놀이방(24시간 테스트용)','N','30','3','00:00','23:59','100000',
     'a135ee9a276f4edf81d6e1b6b9d31e39','7818b580e3f340498b97f50e0e801ff8','7c94187b346f4727a0f2478fdc53064f', null, null, 'LIMIT');

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



-- for test
update fintics_broker set broker_client_properties='
production=false
apiUrl=https://openapivts.koreainvestment.com:29443
appKey=PS2ttZ9KmXgyrRWUFmWHTon8qVVJFOD60nUI
appSecret=ul1fmcIF/FfKoo7CSsNf2TLx0wuIyKqrhHR5L4BBwTRHMKfuRfcwszIIL7qwSzs0cVU/wfvNgAWa/UamyRvDIFwqb3+XmAHP4aUrXlyAjY51F//qMLCCGIJGVzwl/j5AmftPeSF7COOZv0Y1H+htoaR4a9YEGEKD07xmDHCV2h3gLXEDLxo=
accountNo=50112058-01
'
where broker_id in ('ca5f55cd88694715bcb4c478710d9a68','961eb9c68c9547ce9ae61bbe3be7f037');

update fintics_strategy set script = '
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.model.TradeAsset
import org.oopscraft.fintics.model.Strategy
import org.oopscraft.fintics.trade.strategy.StrategyResult
import org.oopscraft.fintics.trade.strategy.StrategyResult.Action
import org.oopscraft.fintics.trade.Tools
import org.oopscraft.fintics.indicator.*

def ohlcvs = tradeAsset.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def ohlcv = ohlcvs.first()
def smas = Tools.indicators(ohlcvs, SmaContext.of(5));
def sma = smas.first()

def message = """
ohlcv: ${ohlcv}
sma: ${sma}
"""
tradeAsset.setMessage(message)
def context = tradeAsset.getContext()
context.put("prev_time", dateTime.toLocalTime());

if (ohlcv.close > sma.value) {
    return StrategyResult.of(Action.BUY, 1.0, "buy")
}
if (ohlcv.close < sma.value) {
    return StrategyResult.of(Action.SELL, 0.0, "sell")
}
'
where strategy_id = '7c94187b346f4727a0f2478fdc53064f';

update fintics_trade
set `interval` = '10',
    threshold = '1',
    order_kind='MARKET'
where trade_id in ('06c228451ce0400fa57bb36f0568d7cb','7af6bc641eef4254b12dd9fa1d43384d')
;


