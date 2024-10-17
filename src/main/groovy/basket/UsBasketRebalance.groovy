import groovy.json.JsonSlurper
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.jsoup.Jsoup
import org.oopscraft.fintics.basket.BasketRebalanceAsset
import org.oopscraft.fintics.model.Asset

/**
 * item
 */
@Builder
@ToString
class Item {
    String symbol
    String name
    String etfSymbol
    BigDecimal score
}

/**
 * gets ETF holdings
 * @param etfSymbol
 * @return
 */
static List<Item> getEtfItems(etfSymbol) {
    def url= new URL("https://finviz.com/api/etf_holdings/${etfSymbol}/top_ten")
    def responseJson= url.text
    def jsonSlurper = new JsonSlurper()
    def responseMap = jsonSlurper.parseText(responseJson)
    def top10Holdings = responseMap.get('rowData')
    // 결과 값이 이상할 경우(10개 이하인 경우) 에러 처리
    if (top10Holdings == null || top10Holdings.size() < 10) {
        throw new NoSuchElementException("Top 10 holdings data is incomplete or missing - ${etfSymbol}")
    }
    return top10Holdings.collect{
        Item.builder()
                .symbol(it.ticker as String)
                .name(it.name as String)
                .etfSymbol(etfSymbol)
                .build()
    }
}

//=======================================
// defines
//=======================================
List<Item> candidateItems = []

//=======================================
// collect etf items
//=======================================
// ETF list
def etfSymbols = [
        // index ETF
        'SPY',      // SPDR S&P 500 ETF Trust
        'QQQ',      // Invesco QQQ Trust
        'DIA',      // SPDR Dow Jones Industrial Average ETF Trust
        'VTI',      // Vanguard Total Stock Market Index Fund ETF Shares
        // strategy ETF
        'JEPI',     // JPMorgan Equity Premium Income ETF
        'JEPQ',     // JPMorgan Nasdaq Equity Premium Income ETF
        'DGRW',     // WisdomTree U.S. Quality Dividend Growth Fund
        'DIVO',     // Amplify CWP Enhanced Dividend Income ETF
        'MOAT',     // VanEck Morningstar Wide Moat ETF
        'SCHD',     // Schwab U.S. Dividend Equity ETF
        'QUAL',     // iShares MSCI USA Quality Factor ETF
        'SPHQ',     // Invesco S&P 500 Quality ETF
        'VIG',      // Vanguard Dividend Appreciation Index Fund ETF Shares
        'VTV',      // Vanguard Value Index Fund ETF Shares
        'MTUM',     // iShares MSCI USA Momentum Factor ETF
        'VUG',      // Vanguard Growth ETF
        'IWF',      // iShares Russell 1000 Growth ETF
        'COWZ',     // Pacer US Cash Cows 100 ETF
        // sector ETF
        'XLK',      // The Technology Select Sector SPDR Fund
        'VGT',      // Vanguard Information Technology Index Fund ETF Shares
        'IYW',      // iShares U.S. Technology ETF
        'SOXX',     // iShares Semiconductor ETF
        'SMH',      // VanEck Semiconductor ETF
        'XLV',      // SPDR Select Sector Fund - Health Care
        'XBI',      // SPDR Series Trust SPDR S&P Biotech ETF
        'GRID',     // First Trust NASDAQ Clean Edge Smart Grid Infrastructure Index Fund
        'XLI',      // The Industrial Select Sector SPDR Fund
        'XAR',      // SPDR S&P Aerospace & Defense ETF
        'ITA',      // iShares U.S. Aerospace & Defense ETF
        'XLF',      // SPDR Select Sector Fund - Financial
        'XLU',      // SPDR Select Sector Fund - Utilities
        'XLY',      // SPDR Select Sector Fund - Consumer Discretionary
        'XLB',      // The Materials Select Sector SPDR Fund
]
etfSymbols.each{
    def etfItems = getEtfItems(it)
    println ("etfItems[${it}]: ${etfItems}")
    candidateItems.addAll(etfItems)
}

//========================================
// distinct items
//========================================
candidateItems = candidateItems
        .groupBy { it.symbol }
        .collect { symbol, items ->
            def item = items[0]
            def etfSymbol = items*.etfSymbol.join(',')
            return Item.builder()
                    .symbol(item.symbol)
                    .name(item.name)
                    .etfSymbol(etfSymbol)
                    .build();
        }
log.info("candidateItems: ${candidateItems}")

//=========================================
// filter
//=========================================
List<Item> finalItems = candidateItems.findAll {
    // checks already fixed
    boolean alreadyFixed = basket.getBasketAssets().findAll{balanceAsset ->
        balanceAsset.getSymbol() == it.symbol && balanceAsset.isFixed()
    }
    if (alreadyFixed) {
        return false
    }

    // check asset
    Asset asset = assetService.getAsset("US.${it.symbol}").orElse(null)
    if (asset == null) {
        return false
    }

    // STOCK 이 아니면 제외
    if (asset.getType() != "STOCK") {
        return false
    }

    //  ROE
    def roes = asset.getAssetMetas('ROE').collect{new BigDecimal(it.value?:'0.0')}
    def roe = roes.find{true}?:0.0 as BigDecimal
    if (roe < 5.0) {    // ROE 5 이하는 수익성 없는 회사로 제외
        return false
    }

    // ROA
    def roas = asset.getAssetMetas('ROA').collect{new BigDecimal(it.value?:'0.0')}
    def roa = roas.find{true}?:0.0
    if (roa < 0.0) {    // ROA 0 이하는 부채 비율이 높은 경우 일수 있음 으로 제외
        return false
    }

    // PER
    def pers = asset.getAssetMetas('PER').collect{new BigDecimal(it.value?:'100.0')}
    def per = pers.find{true}?:100.0

    // dividendYield
    def dividendYields = asset.getAssetMetas("Dividend Yield").collect{ new BigDecimal(it.value?:'0.0')}
    def dividendYield = dividendYields.find{true}?:0.0

    // score
    it.score = roe + dividendYield

    // return
    return it
}
log.info("finalItems: ${finalItems}")

//=========================================
// sort by score
//=========================================
def maxAssetCount = 50
def holdingWeightPerAsset = 2.0
def fixedAssetCount = basket.getBasketAssets().findAll{it.enabled && it.fixed}.size()
def targetAssetCount = (maxAssetCount - fixedAssetCount) as Integer
finalItems = finalItems
        .sort{ -(it.score?:0)}
        .take(targetAssetCount)

//=========================================
// return
//=========================================
List<BasketRebalanceAsset> basketRebalanceResults = finalItems.collect{
    BasketRebalanceAsset.of(it.symbol, it.name, holdingWeightPerAsset, it.etfSymbol)
}
log.info("basketRebalanceResults: ${basketRebalanceResults}")
return basketRebalanceResults
