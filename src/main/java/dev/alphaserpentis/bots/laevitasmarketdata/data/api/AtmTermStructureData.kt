package dev.alphaserpentis.bots.laevitasmarketdata.data.api

data class AtmTermStructureData(
    val date: Long,
    val data: Data
) {
    data class Data(
        val tenor: List<Tenor>,
        val expiry: List<Expiry>
    ) {
        data class Tenor(
            val ttm: Double,
            val iv: Double
        )

        data class Expiry(
            val expiry: String,
            val iv: Double
        )
    }
}
