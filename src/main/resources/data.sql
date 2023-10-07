-- core_menu
delete from `core_menu_i18n`;
delete from `core_menu`;

-- fintics_asset
insert into `fintics_asset`
    (`symbol`, `name`, `type`, `country`, `currency`) values
    ('005930','삼성전자','STOCK','KR','KRW');
insert into `fintics_asset`
    (`symbol`, `name`, `type`, `country`, `currency`) values
    ('069500','KODEX 200','ETF','KR','KRW');

-- fintics_trade
insert into `fintics_trade`
    (`trade_id`,`name`,`enabled`,`client_properties`) values
    ('06c228451ce0400fa57bb36f0568d7cb','Test Trade','false','production: false
apiUrl=https://openapivts.koreainvestment.com:29443
appKey=PSxH2bEQzlbPZSOG0EBu2JkQR24LkX8w7rdh
appSecret=3YxieOOGqvPFwomXiX99UVjNezweG79sXgmp/2GKzGAPHjG53SCqYKrNiKBfyEDFjJIaPsGQT70CdCxDqYKaqXPN16BjkRqZOhYtNJJC1xNJlUBvL3mj7OZdAq0zp+wezm8xGIsjBim0P0Ej+rGZM2nQWNSFWwPde2n3zgv4M7J3nr/kNDI=
accountNo=50096055-01
');

-- fintics_trade_asset
insert into `fintics_trade_asset`
    (`trade_id`,`symbol`, `enabled`) values
    ('06c228451ce0400fa57bb36f0568d7cb','005930','true');
insert into `fintics_trade_asset`
    (`trade_id`,`symbol`, `enabled`) values
    ('06c228451ce0400fa57bb36f0568d7cb','069500','true');
