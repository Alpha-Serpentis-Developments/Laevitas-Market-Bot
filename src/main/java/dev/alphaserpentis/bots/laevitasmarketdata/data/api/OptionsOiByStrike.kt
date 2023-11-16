package dev.alphaserpentis.bots.laevitasmarketdata.data.api

import com.google.gson.annotations.SerializedName

data class OptionsOiByStrike(
    val date: Long,
    val data: List<OpenInterestData>
) {
    data class OpenInterestData(
        val strike: Double,
        @SerializedName("c")
        val callOi: Double,
        @SerializedName("p")
        val putOi: Double,
        @SerializedName("notional_c")
        val callNotional: Double,
        @SerializedName("notional_p")
        val putNotional: Double,
        @SerializedName("premium_c")
        val premiumCall: Double,
        @SerializedName("premium_p")
        val premiumPut: Double,
        val intrinsic: Double?
    )
}
