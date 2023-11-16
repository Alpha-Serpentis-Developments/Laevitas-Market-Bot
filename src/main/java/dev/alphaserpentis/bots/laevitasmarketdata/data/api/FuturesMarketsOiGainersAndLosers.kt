package dev.alphaserpentis.bots.laevitasmarketdata.data.api

import com.google.gson.annotations.SerializedName

data class FuturesMarketsOiGainersAndLosers(
    val date: Long,
    val data: List<Data>
) {
    data class Data(
        val market: String,
        @SerializedName("open_interest_change")
        val oiChange: Double,
        @SerializedName("open_interest_change_perc")
        val oiChangePercent: Double,
        @SerializedName("open_interest_notional_change")
        val oiNotionalChange: Double,
        @SerializedName("open_interest_notional_change_perc")
        val oiNotionalChangePercent: Double
    )
}
