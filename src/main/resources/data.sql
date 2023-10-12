-- delete not used data
delete from `core_menu_i18n`;
delete from `core_menu`;
delete from `core_git`;

-- core_authority
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('TRADE','Y','Trade Access Authority');
insert into `core_authority` (`authority_id`,`system_required`,`authority_name`) values ('TRADE_EDIT','Y','Trade Edit Authority');

-- core_role_authority
insert into `core_role_authority` (`role_id`,`authority_id`) values ('ANONYMOUS', 'TRADE');

-- fintics_trade
insert into `fintics_trade`
    (`trade_id`,`name`,`enabled`,`interval`,`client_type`,`client_properties`) values
    ('06c228451ce0400fa57bb36f0568d7cb','한국투자증권 모의투자 #1','Y','30',
     'org.oopscraft.fintics.client.kis.KisClient','production=false
apiUrl=https://openapivts.koreainvestment.com:29443
appKey=PSxH2bEQzlbPZSOG0EBu2JkQR24LkX8w7rdh
appSecret=3YxieOOGqvPFwomXiX99UVjNezweG79sXgmp/2GKzGAPHjG53SCqYKrNiKBfyEDFjJIaPsGQT70CdCxDqYKaqXPN16BjkRqZOhYtNJJC1xNJlUBvL3mj7OZdAq0zp+wezm8xGIsjBim0P0Ej+rGZM2nQWNSFWwPde2n3zgv4M7J3nr/kNDI=
accountNo=50096055-01
');

-- fintics_trade_asset
insert into `fintics_trade_asset`
    (`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','069500','KODEX 200','ETF','Y','10');
insert into `fintics_trade_asset`
    (`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','114800','KODEX 인버스','ETF','Y','10');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','229200','KODEX 코스닥150','ETF','Y','10');
insert into `fintics_trade_asset`
(`trade_id`,`symbol`,`name`,`type`, `enabled`, `hold_ratio`) values
    ('06c228451ce0400fa57bb36f0568d7cb','251340','KODEX 코스닥150선물인버스','ETF','Y','10');
