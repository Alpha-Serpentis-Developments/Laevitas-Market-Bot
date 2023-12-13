package dev.alphaserpentis.bots.laevitasmarketdata.api

import dev.alphaserpentis.bots.laevitasmarketdata.data.api.*
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface LaevitasEndpoints {
    @Headers("Content-Type: application/json")
    @GET("/api/catalog/v2")
    fun getCatalog(): Single<Catalog>

    @Headers("Content-Type: application/json")
    @GET("/analytics/options/model_charts/term_structure_atm/{currency}")
    fun getAtmTermStructure(
        @Header("apiKey") apiKey: String,
        @Path("currency") currency: String
    ): Single<AtmTermStructureData>

    @Headers("Content-Type: application/json")
    @GET("/analytics/options/model_charts/vol_run/{currency}/all")
    fun getVolRun(
        @Header("apiKey") apiKey: String,
        @Path("currency") currency: String
    ): Single<VolRun>

    @Headers("Content-Type: application/json")
    @GET("/analytics/options/model_charts/skew/{currency}/{maturity}/{type}")
    fun getSkewData(
        @Header("apiKey") apiKey: String,
        @Path("currency") currency: String,
        @Path("maturity") maturity: String,
        @Path("type") type: String
    ): Single<SkewData>

    @Headers("Content-Type: application/json")
    @GET("/analytics/options/model_charts/skew/{currency}/{maturity}/{type}")
    fun getSkewDataAll(
        @Header("apiKey") apiKey: String,
        @Path("currency") currency: String,
        @Path("type") type: String,
        @Path("maturity") maturity: String = "all"
    ): Single<SkewDataAllMaturities>

    @Headers("Content-Type: application/json")
    @GET("/analytics/options/eth-btc_atm_iv_term_structure")
    fun getEthBtcAtmIvTermStructure(
        @Header("apiKey") apiKey: String
    ): Single<EthBtcAtmIvTermStructure>

    @Headers("Content-Type: application/json")
    @GET("/analytics/options/oi_strike_all/{market}/{currency}")
    fun getOptionsOiByStrike(
        @Header("apiKey") apiKey: String,
        @Path("market") market: String,
        @Path("currency") currency: String
    ): Single<OptionsOiByStrike>

    @Headers("Content-Type: application/json")
    @GET("/analytics/options/oi_strike/{market}/{currency}/{maturity}")
    fun getOptionsOiByStrike(
        @Header("apiKey") apiKey: String,
        @Path("market") market: String,
        @Path("currency") currency: String,
        @Path("maturity") maturity: String
    ): Single<OptionsOiByStrike>

    @Headers("Content-Type: application/json")
    @GET("/analytics/options/v_strike_all/{market}/{currency}")
    fun getOptionsVolumeByStrike(
        @Header("apiKey") apiKey: String,
        @Path("market") market: String,
        @Path("currency") currency: String
    ): Single<OptionsVolumeByStrike>

    @Headers("Content-Type: application/json")
    @GET("/analytics/options/v_strike/{market}/{currency}/{maturity}")
    fun getOptionsVolumeByStrike(
        @Header("apiKey") apiKey: String,
        @Path("market") market: String,
        @Path("currency") currency: String,
        @Path("maturity") maturity: String
    ): Single<OptionsVolumeByStrike>

    @Headers("Content-Type: application/json")
    @GET("/analytics/options/top_traded_option/{market}/{currency}")
    fun getTopTradedOptions(
        @Header("apiKey") apiKey: String,
        @Path("market") market: String,
        @Path("currency") currency: String
    ): Single<TopTradedOptions>

    @Headers("Content-Type: application/json")
    @GET("/analytics/futures/perpetual_funding/{currency}")
    fun getPerpetualFunding(
        @Header("apiKey") apiKey: String,
        @Path("currency") currency: String
    ): Single<PerpetualFunding>

    @Headers("Content-Type: application/json")
    @GET("/analytics/futures/futures_curve/{currency}/{market}")
    fun getFuturesCurve(
        @Header("apiKey") apiKey: String,
        @Path("currency") currency: String,
        @Path("market") market: String
    ): Single<FuturesCurve>

    @Headers("Content-Type: application/json")
    @GET("/analytics/futures/futures_curve/{currency}")
    fun getFuturesCurve(
        @Header("apiKey") apiKey: String,
        @Path("currency") currency: String
    ): Single<FuturesCurve>

    @Headers("Content-Type: application/json")
    @GET("/analytics/futures/oi_breakdown/{currency}/{type}")
    fun getFuturesOiBreakdown(
        @Header("apiKey") apiKey: String,
        @Path("currency") currency: String,
        @Path("type") type: String
    ): Single<FuturesOiBreakdown>

    @Headers("Content-Type: application/json")
    @GET("/analytics/futures/volume_breakdown/{currency}/{type}")
    fun getFuturesVolumeBreakdown(
        @Header("apiKey") apiKey: String,
        @Path("currency") currency: String,
        @Path("type") type: String
    ): Single<FuturesVolumeBreakdown>

    @Headers("Content-Type: application/json")
    @GET("/analytics/futures/markets_oi_gainers_and_losers/{currency}/{option}/{param}")
    fun getFuturesMarketsOiGainersAndLosers(
        @Header("apiKey") apiKey: String,
        @Path("currency") currency: String,
        @Path("option") option: String,
        @Path("param") param: String
    ): Single<FuturesMarketsOiGainersAndLosers>

    @Headers("Content-Type: application/json")
    @GET("/analytics/derivs/price_gainers/{market}/{type}/{period}")
    fun getDerivsPriceGainers(
        @Header("apiKey") apiKey: String,
        @Path("market") market: String,
        @Path("type") type: String,
        @Path("period") period: String
    ): Single<DerivsPriceGainers>
}
