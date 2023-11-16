package dev.alphaserpentis.bots.laevitasmarketdata.data.api

data class DerivsPriceGainers(
    val date: Long,
    val data: List<Data>
) {
    data class Data(
        val symbol: String,
        val priceChange: Double
    )
}
