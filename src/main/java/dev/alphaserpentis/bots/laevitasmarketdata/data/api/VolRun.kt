package dev.alphaserpentis.bots.laevitasmarketdata.data.api

import com.google.gson.annotations.SerializedName

data class VolRun(
    val date: Long,
    val data: List<Data>
) {
    data class Data(
        val expiry: String,
        val forward: Double,
        @SerializedName("atm_spot_iv")
        val atmSpotIv: Double,
        @SerializedName("skew_10d")
        val skew10d: Double,
        @SerializedName("skew_15d")
        val skew15d: Double,
        @SerializedName("skew_25d")
        val skew25d: Double,
        @SerializedName("skew_35d")
        val skew35d: Double,
        @SerializedName("rr_10d")
        val riskReversal10d: Double,
        @SerializedName("rr_15d")
        val riskReversal15d: Double,
        @SerializedName("rr_25d")
        val riskReversal25d: Double,
        @SerializedName("rr_35d")
        val riskReversal35d: Double,
        @SerializedName("fly_10d")
        val butterfly10d: Double,
        @SerializedName("fly_15d")
        val butterfly15d: Double,
        @SerializedName("fly_25d")
        val butterfly25d: Double,
        @SerializedName("fly_35d")
        val butterfly35d: Double
    )
}
