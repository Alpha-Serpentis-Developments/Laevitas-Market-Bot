package dev.alphaserpentis.bots.laevitasmarketdata.data.api

import com.google.gson.annotations.SerializedName

data class OptionsVolumeByStrike(
    val date: Long,
    val data: List<OptionsVolumeData>
) {
    data class OptionsVolumeData(
        val strike: Double,
        @SerializedName("c")
        val callVolume: Double,
        @SerializedName("p")
        val putVolume: Double,
        @SerializedName("notional_c")
        val callNotional: Double,
        @SerializedName("notional_p")
        val putNotional: Double
    )
}
