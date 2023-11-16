package dev.alphaserpentis.bots.laevitasmarketdata.data.api

import com.google.gson.annotations.SerializedName

/**
 * @see <a href="https://api.laevitas.ch/swagger/#/Analytics%20Options/getModelSkewCharts">Model Skew Charts</a>
 */
data class SkewData(
    val date: Long,
    val data: List<SkewDataEntry>
) {
    data class SkewDataEntry(
        @SerializedName(value="strike", alternate=["delta"])
        val sharedValue: Double,
        val iv: Double
    )
}
