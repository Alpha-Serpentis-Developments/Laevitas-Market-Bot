package dev.alphaserpentis.bots.laevitasmarketdata.data.api

import com.google.gson.annotations.SerializedName

data class PerpetualFunding(
    val date: Long,
    val data: List<PerpetualFundingEntry>
) {
    data class PerpetualFundingEntry(
        val market: String,
        val symbol: String,
        val funding: Double,
        val yield: Double,
        @SerializedName("next_fr")
        val nextFundingRate: Double?
    )
}
