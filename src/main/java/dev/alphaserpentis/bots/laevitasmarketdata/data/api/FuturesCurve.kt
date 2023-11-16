package dev.alphaserpentis.bots.laevitasmarketdata.data.api

data class FuturesCurve(
    val date: Long,
    val data: List<FuturesCurveEntry>
) {
    data class FuturesCurveEntry(
        val market: String,
        val maturity: String,
        val value: Double
    )
}
