package dev.alphaserpentis.bots.laevitasmarketdata.data.api

import com.google.gson.annotations.SerializedName

data class Catalog(
    val params: MainParam,
    @SerializedName("api_list")
    val apiList: HashSet<ApiList>
) {
    data class MainParam(
        val maturities: SubParam,
        val strikes: SubParam,
        val currency: CurrencyParam
    ) {
        data class SubParam(
            val name: String,
            val data: Map<String, Map<String, List<String>>>
        )

        data class CurrencyParam(
            val name: String,
            val data: List<String>
        )
    }

    data class ApiList(
        val description: String,
        val methodName: String,
        val path: String,
        val format: String,
        @SerializedName("path_params")
        val params: HashMap<String, HashSet<String>>?
    )
}
