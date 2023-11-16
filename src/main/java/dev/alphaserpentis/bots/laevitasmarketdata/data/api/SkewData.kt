package dev.alphaserpentis.bots.laevitasmarketdata.data.api

/**
 * @see <a href="https://api.laevitas.ch/swagger/#/Analytics%20Options/getModelSkewCharts">Model Skew Charts</a>
 */
data class SkewData(
    val date: Long,
    val data: Map<String, List<SkewDataEntry>>
) {
    data class SkewDataEntry(
        val strike: Double,
        val iv: Double
    )
}
