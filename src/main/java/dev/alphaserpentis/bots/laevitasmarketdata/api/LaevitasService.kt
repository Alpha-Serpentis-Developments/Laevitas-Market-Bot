package dev.alphaserpentis.bots.laevitasmarketdata.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.rxjava3.core.Single
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class LaevitasService(private val apiKey: String) {
    private val gson: Gson = GsonBuilder().setLenient().serializeNulls().create()
    val api: LaevitasEndpoints = Retrofit.Builder()
        .baseUrl(REST_API_URL)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(LaevitasEndpoints::class.java)

    fun atmTermStructure(currency: String) = execute(api.getAtmTermStructure(apiKey, currency))

    fun volRun(currency: String, maturity: String) = execute(api.getVolRun(apiKey, currency, maturity))

    fun skewData(
        currency: String,
        maturity: String,
        type: String
    ) = execute(api.getSkewData(apiKey, currency, maturity, type))

    fun ethBtcAtmIvTermStructure() = execute(api.getEthBtcAtmIvTermStructure(apiKey))

    fun optionsOiByStrike(
        market: String,
        currency: String
    ) = execute(api.getOptionsOiByStrike(apiKey, market, currency))

    fun optionsOiByStrike(
        market: String,
        currency: String,
        maturity: String
    ) = execute(api.getOptionsOiByStrike(apiKey, market, currency, maturity))

    fun optionsVolumeByStrike(
        market: String,
        currency: String
    ) = execute(api.getOptionsVolumeByStrike(apiKey, market, currency))

    fun optionsVolumeByStrike(
        market: String,
        currency: String,
        maturity: String
    ) = execute(api.getOptionsVolumeByStrike(apiKey, market, currency, maturity))

    fun topTradedOptions(
        market: String,
        currency: String
    ) = execute(api.getTopTradedOptions(apiKey, market, currency))

    fun perpetualFunding(currency: String) = execute(api.getPerpetualFunding(apiKey, currency))

    fun futuresCurve(
        currency: String,
        market: String?
    ) = if (market == null)
        execute(api.getFuturesCurve(apiKey, currency))
    else
        execute(api.getFuturesCurve(apiKey, currency, market))

    fun futuresOiBreakdown(currency: String, type: String) = execute(api.getFuturesOiBreakdown(apiKey, currency, type))

    fun futuresVolumeBreakdown(
        currency: String,
        type: String
    ) = execute(api.getFuturesVolumeBreakdown(apiKey, currency, type))

    fun futuresMarketsOiGainersAndLosers(
        currency: String,
        option: String,
        param: String
    ) = execute(api.getFuturesMarketsOiGainersAndLosers(apiKey, currency, option, param))

    fun derivsPriceGainers(
        market: String,
        type: String,
        period: String
    ) = execute(api.getDerivsPriceGainers(apiKey, market, type, period))

    companion object {
        const val REST_API_URL = "https://api.laevitas.ch"

        fun <T : Any> execute(call: Single<T>): T {
            return try {
                call.blockingGet()
            } catch (e: HttpException) {
                // TODO: Replace with SLF4J
                throw RuntimeException(e)
            }
        }
    }
}
