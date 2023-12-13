package dev.alphaserpentis.bots.laevitasmarketdata.data.api

import com.google.gson.annotations.SerializedName

data class SkewDataAllMaturities(
    val date: Long,
    val data: Map<String, List<SkewDataEntry>>
) {
    data class SkewDataEntry(
        @SerializedName(value="strike", alternate=["delta"])
        val sharedValue: Double,
        val iv: Double
    )
}
