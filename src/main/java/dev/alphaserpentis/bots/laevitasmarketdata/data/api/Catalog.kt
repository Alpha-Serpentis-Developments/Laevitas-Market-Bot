package dev.alphaserpentis.bots.laevitasmarketdata.data.api

data class Catalog(
    val params: MainParam
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
}
