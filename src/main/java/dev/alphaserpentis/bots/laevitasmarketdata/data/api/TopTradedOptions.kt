package dev.alphaserpentis.bots.laevitasmarketdata.data.api

import com.google.gson.annotations.SerializedName

data class TopTradedOptions(
    val date: Long,
    val data: List<TopTradedOptionsEntry>
) {
    data class TopTradedOptionsEntry(
        val volume: Double,
        val instrument: String,
        @SerializedName("volume_usd")
        val volumeUsd: Double,
        @SerializedName("mark_price")
        val markPrice: Double,
        val premium: Double
    )
}
