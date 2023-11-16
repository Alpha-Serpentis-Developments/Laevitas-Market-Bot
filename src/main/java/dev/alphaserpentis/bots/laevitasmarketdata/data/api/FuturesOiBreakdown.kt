package dev.alphaserpentis.bots.laevitasmarketdata.data.api

data class FuturesOiBreakdown(
    val date: Long,
    val data: BreakdownData
) {
    data class BreakdownData(
        val all: FuturesOiForMarket,
        val future: FuturesOiForMarket,
        val perpetual: FuturesOiForMarket
    ) {
        data class FuturesOiForMarket(
            val usd: Map<String, Double>,
            val notional: Map<String, Double>
        )
    }
}
