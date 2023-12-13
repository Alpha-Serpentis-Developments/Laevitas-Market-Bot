package dev.alphaserpentis.bots.laevitasmarketdata.data.api

data class FuturesVolumeBreakdown(
    val date: Long,
    val data: BreakdownData
) {
    data class BreakdownData(
        val all: FuturesVolumeForMarket,
        val future: FuturesVolumeForMarket?,
        val perpetual: FuturesVolumeForMarket
    ) {
        data class FuturesVolumeForMarket(
            val usd: Map<String, Double>,
            val notional: Map<String, Double>
        )
    }
}
